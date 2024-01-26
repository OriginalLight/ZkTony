using System.Drawing;
using System.Drawing.Imaging;
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

namespace Exposure.Api.Services;

public class PictureService : BaseService<Picture>, IPictureService
{
    private readonly IUserService _user;
    private readonly IDbContext context;

    public PictureService(IDbContext dbContext, IUserService user) : base(dbContext)
    {
        context = dbContext;
        _user = user;
    }

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
        var lower = await context.db.Queryable<User>().Where(u => u.Role > logged.Role).ToListAsync();
        var users = lower.Append(logged).ToList();
        var ids = users.Select(u => u.Id).ToList();
        return await context.db.Queryable<Picture>()
            .Where(p => p.IsDelete == dto.IsDeleted)
            .WhereIF(!string.IsNullOrEmpty(dto.Name), p => p.Name.Contains(dto.Name))
            .WhereIF(dto.StartTime != null, p => p.CreateTime >= dto.StartTime)
            .WhereIF(dto.EndTime != null, p => p.CreateTime <= dto.EndTime)
            .Where(p => ids.Contains(p.UserId))
            .OrderBy(p => p.CreateTime, OrderByType.Desc)
            .ToPageListAsync(dto.Page, dto.Size, total);
    }

    /// <summary>
    ///     添加并返回实体
    /// </summary>
    /// <param name="picture"></param>
    /// <returns></returns>
    public Task<Picture> AddReturnModel(Picture picture)
    {
        var id = context.db.Insertable(picture).ExecuteReturnIdentity();
        return context.db.Queryable<Picture>().InSingleAsync(id);
    }

    /// <summary>
    ///     根据id查询
    /// </summary>
    /// <param name="ids"></param>
    /// <returns></returns>
    public async Task<List<Picture>> GetByIds(object[] ids)
    {
        return await context.db.Queryable<Picture>().Where(p => ids.Contains(p.Id)).ToListAsync();
    }

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
        var bitmap = mat.ToBitmap();

        var savePath = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.MyPictures), "Exposure");
        if (!Directory.Exists(savePath)) Directory.CreateDirectory(savePath);
        var date = DateTime.Now.ToString("yyyyMMddHHmmss");
        var filePath = Path.Combine(savePath, $"{date}.png");
        bitmap.Save(filePath, ImageFormat.Png);

        return await AddReturnModel(new Picture
        {
            UserId = _user.GetLogged()?.Id ?? 0,
            Name = date,
            Path = filePath,
            Width = bitmap.Width,
            Height = bitmap.Height,
            Type = 2,
            ExposureTime = 0,
            ExposureGain = 0,
            BlackLevel = 0,
            IsDelete = false,
            CreateTime = DateTime.Now,
            UpdateTime = DateTime.Now,
            DeleteTime = DateTime.Now
        });
    }

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
        // 保存图片 image to bitmap
        var savePath = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.MyPictures), "Exposure");
        if (!Directory.Exists(savePath)) Directory.CreateDirectory(savePath);
        var date = DateTime.Now.ToString("yyyyMMddHHmmss");
        var filePath = Path.Combine(savePath, $"{date}.png");

        await image.SaveAsPngAsync(filePath);

        return await AddReturnModel(new Picture
        {
            UserId = _user.GetLogged()?.Id ?? 0,
            Name = date,
            Path = filePath,
            Width = image.Width,
            Height = image.Height,
            Type = pic.Type,
            ExposureTime = pic.ExposureTime,
            ExposureGain = pic.ExposureGain,
            BlackLevel = pic.BlackLevel,
            IsDelete = false,
            CreateTime = DateTime.Now,
            UpdateTime = DateTime.Now,
            DeleteTime = DateTime.Now
        });
    }
}