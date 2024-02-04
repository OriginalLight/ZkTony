using System.Drawing;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Contracts.SqlSugar;
using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using OpenCvSharp;
using OpenCvSharp.Extensions;
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.Processing;
using SqlSugar;
using Image = SixLabors.ImageSharp.Image;
using Size = OpenCvSharp.Size;

namespace Exposure.Api.Services;

public class PictureService : BaseService<Picture>, IPictureService
{
    private readonly IUserService _user;
    private readonly IDbContext _context;
    
    #region 构造函数

    public PictureService(IDbContext dbContext, IUserService user) : base(dbContext)
    {
        _context = dbContext;
        _user = user;
    }

    #endregion

    #region 分页查询

    /// <summary>
    ///     分页查询
    /// </summary>
    /// <param name="dto"></param>
    /// <param name="total"></param>
    /// <returns></returns>
    public async Task<List<Picture>> GetByPage(PictureQueryDto dto, RefAsync<int> total)
    {
        var logged = _user.GetLogged();
        if (logged == null) return new List<Picture>();
        var lower = await _context.db.Queryable<User>().Where(u => u.Role > logged.Role).ToListAsync();
        var users = lower.Append(logged).ToList();
        var ids = users.Select(u => u.Id).ToList();
        return await _context.db.Queryable<Picture>()
            .Where(p => p.IsDelete == dto.IsDeleted)
            .WhereIF(!string.IsNullOrEmpty(dto.Name), p => dto.Name != null && p.Name.Contains(dto.Name))
            .WhereIF(dto.StartTime != null, p => p.CreateTime >= dto.StartTime)
            .WhereIF(dto.EndTime != null, p => p.CreateTime <= dto.EndTime!.Value.AddDays(1))
            .Where(p => ids.Contains(p.UserId))
            .OrderBy(p => p.CreateTime, OrderByType.Desc)
            .ToPageListAsync(dto.Page, dto.Size, total);
    }

    #endregion

    #region 添加并返回实体

    /// <summary>
    ///     添加并返回实体
    /// </summary>
    /// <param name="picture"></param>
    /// <returns></returns>
    public Task<Picture> AddReturnModel(Picture picture)
    {
        var id = _context.db.Insertable(picture).ExecuteReturnIdentity();
        return _context.db.Queryable<Picture>().InSingleAsync(id);
    }

    #endregion

    #region 根据id查询

    /// <summary>
    ///     根据id查询
    /// </summary>
    /// <param name="ids"></param>
    /// <returns></returns>
    public async Task<List<Picture>> GetByIds(object[] ids)
    {
        return await _context.db.Queryable<Picture>().Where(p => ids.Contains(p.Id)).ToListAsync();
    }

    #endregion

    #region 合并图片

    /// <summary>
    ///     合并图片
    /// </summary>
    /// <param name="list"></param>
    /// <returns></returns>
    public async Task<Picture> Combine(List<Picture> list)
    {
        var bitmap1 = new Bitmap(list[0].Path);
        var bitmap2 = new Bitmap(list[1].Path);
        var mat1 = bitmap1.ToMat();
        var mat2 = bitmap2.ToMat();
        var mat = new Mat(list[0].Height, list[1].Width, MatType.CV_8UC3, new Scalar(0));
        Cv2.Add(mat1, mat2, mat);

        // 保存图片
        var myPictures = Environment.GetFolderPath(Environment.SpecialFolder.MyPictures);
        var savePath = Path.Combine(myPictures, "Exposure");
        if (!Directory.Exists(savePath)) Directory.CreateDirectory(savePath);
        var date = DateTime.Now.ToString("yyyyMMddHHmmss");
        var filePath = Path.Combine(savePath, $"{date}.png");
        mat.SaveImage(filePath);

        // 保存缩略图
        var thumbnail = mat.Clone();
        Cv2.Resize(mat, thumbnail, new Size(500, 500));
        var thumbnailPath = Path.Combine(myPictures, "Thumbnail");
        if (!Directory.Exists(thumbnailPath)) Directory.CreateDirectory(thumbnailPath);
        var thumbnailFilePath = Path.Combine(thumbnailPath, $"{date}.jpg");
        thumbnail.SaveImage(thumbnailFilePath);

        var width = mat.Width;
        var height = mat.Height;
        // 释放资源
        mat1.Dispose();
        mat2.Dispose();
        mat.Dispose();
        thumbnail.Dispose();

        return await AddReturnModel(new Picture
        {
            UserId = _user.GetLogged()?.Id ?? 0,
            Name = date,
            Path = filePath,
            Width = width,
            Height = height,
            Type = 2,
            Thumbnail = thumbnailFilePath,
            ExposureTime = 0,
            ExposureGain = 0,
            BlackLevel = 0,
            IsDelete = false,
            CreateTime = DateTime.Now,
            UpdateTime = DateTime.Now,
            DeleteTime = DateTime.Now
        });
    }

    #endregion

    #region 删除

    /// <summary>
    ///     调整图片
    /// </summary>
    /// <param name="pic"></param>
    /// <param name="dto"></param>
    /// <returns></returns>
    public async Task<Picture> Adjust(Picture pic, PictureAdjustDto dto)
    {
        // 使用imageSharp处理图片
        var image = await Image.LoadAsync(pic.Path);
        // 增强亮度
        image.Mutate(x => x.Brightness(dto.Brightness / 100.0f));
        // 增强对比度
        image.Mutate(x => x.Contrast(dto.Contrast / 100.0f));

        if (dto.Invert) image.Mutate(x => x.Invert());
        // 保存图片
        var myPictures = Environment.GetFolderPath(Environment.SpecialFolder.MyPictures);
        var savePath = Path.Combine(myPictures, "Exposure");
        if (!Directory.Exists(savePath)) Directory.CreateDirectory(savePath);
        var date = DateTime.Now.ToString("yyyyMMddHHmmss");
        var filePath = Path.Combine(savePath, $"{date}.png");
        await image.SaveAsPngAsync(filePath);

        var width = image.Width;
        var height = image.Height;

        // 保存缩略图
        image.Mutate(x => x.Resize(500, 500));
        var thumbnailPath = Path.Combine(myPictures, "Thumbnail");
        if (!Directory.Exists(thumbnailPath)) Directory.CreateDirectory(thumbnailPath);
        var thumbnailFilePath = Path.Combine(thumbnailPath, $"{date}.jpg");
        await image.SaveAsJpegAsync(thumbnailFilePath);

        // 释放资源
        image.Dispose();

        return await AddReturnModel(new Picture
        {
            UserId = _user.GetLogged()?.Id ?? 0,
            Name = date,
            Path = filePath,
            Width = width,
            Height = height,
            Type = pic.Type,
            Thumbnail = thumbnailFilePath,
            ExposureTime = pic.ExposureTime,
            ExposureGain = pic.ExposureGain,
            BlackLevel = pic.BlackLevel,
            IsDelete = false,
            CreateTime = DateTime.Now,
            UpdateTime = DateTime.Now,
            DeleteTime = DateTime.Now
        });
    }

    #endregion

    #region 更新

    /// <summary>
    ///     更新
    /// </summary>
    /// <param name="model"></param>
    /// <returns></returns>
    public async Task<bool> Update(PictureUpdateDto model)
    {
        var pic = await _context.db.Queryable<Picture>().InSingleAsync(model.Id);
        if (pic == null) return false;
        if (string.IsNullOrEmpty(model.Name)) return false;
        pic.Name = model.Name;
        var res = await _context.db.Updateable(pic).ExecuteCommandAsync();
        return res > 0;
    }

    #endregion
    
}