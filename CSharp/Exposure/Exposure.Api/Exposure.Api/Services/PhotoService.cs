using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using Exposure.SqlSugar.Contracts;
using Exposure.Utilities;
using OpenCvSharp;
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.Processing;
using Size = OpenCvSharp.Size;

namespace Exposure.Api.Services;

public class PhotoService(
    IDbContext dbContext
) : BaseService<Photo>(dbContext), IPhotoService
{
    private readonly IDbContext _context = dbContext;

    #region 添加并返回实体

    public async Task<Photo> AddReturnModel(Photo photo)
    {
        return await _context.db.Insertable(photo).ExecuteReturnEntityAsync();
    }

    #endregion

    #region 根据id查询

    public async Task<List<Photo>> GetByIds(int[] ids)
    {
        return await _context.db.Queryable<Photo>().Where(p => ids.Contains(p.Id)).ToListAsync();
    }

    #endregion

    #region 根据图集id查询

    public async Task<List<Photo>> GetByAlbumId(int id)
    {
        return await _context.db.Queryable<Photo>().Where(p => p.AlbumId == id).ToListAsync();
    }

    #endregion

    #region 删除多条数据

    public async Task<bool> DeleteByIds(int[] ids)
    {
        return await _context.db.Deleteable<Photo>().In(ids).ExecuteCommandAsync() > 0;
    }

    #endregion

    #region 更新

    public async Task<bool> Update(PhotoUpdateDto model)
    {
        var pic = await _context.db.Queryable<Photo>().InSingleAsync(model.Id);
        if (pic == null) return false;
        if (string.IsNullOrEmpty(model.Name)) return false;
        pic.Name = model.Name;
        var res = await _context.db.Updateable(pic).ExecuteCommandAsync();
        return res > 0;
    }

    #endregion

    #region 调整

    public async Task<Photo?> Adjust(PhotoAdjustDto dto)
    {
        var pic = await GetByPrimary(dto.Id);
        if (pic == null) return null;
        // 使用imageSharp处理图片
        var image = await Image.LoadAsync(pic.Path);
        // 增强亮度
        image.Mutate(x => x.Brightness(dto.Brightness / 100.0f));
        // 增强对比度
        image.Mutate(x => x.Contrast(dto.Contrast / 100.0f));
        // 反色
        if (dto.Invert) image.Mutate(x => x.Invert());

        // 前缀
        var prefix = pic.Type switch
        {
            0 => "L_",
            1 => "D_",
            2 => "C_",
            _ => ""
        };

        var picName = $"{prefix}{DateTime.Now:yyyyMMddHHmmss}";

        // 保存图片
        if (dto.Code == 0)
        {
            var exposure = Path.Combine(FileUtils.Exposure, $"{picName}.png");
            await image.SaveAsPngAsync(exposure);

            var width = image.Width;
            var height = image.Height;

            // 保存缩略图
            image.Mutate(x => x.Resize(500, 500));
            var thumbnail = Path.Combine(FileUtils.Thumbnail, $"{picName}.jpg");
            await image.SaveAsJpegAsync(thumbnail);

            return await AddReturnModel(new Photo
            {
                Name = picName,
                AlbumId = pic.AlbumId,
                Path = exposure,
                Width = width,
                Height = height,
                Type = pic.Type,
                Thumbnail = thumbnail,
                ExposureTime = pic.ExposureTime,
                Gain = pic.Gain
            });
        }

        image.Mutate(x => x.Resize(500, 500));
        var path = Path.Combine(FileUtils.Preview, $"{picName}.jpg");
        await image.SaveAsJpegAsync(path);


        return new Photo
        {
            Name = picName,
            AlbumId = pic.AlbumId,
            Type = pic.Type,
            Thumbnail = path,
            ExposureTime = pic.ExposureTime,
            Gain = pic.Gain
        };
    }

    #endregion

    #region 合并图片

    public async Task<Photo?> Combine(int[] ids)
    {
        var photos = await GetByIds(ids);
        if (photos.Count != 2) return null;
        if (photos[0].Width != photos[1].Width || photos[0].Height != photos[1].Height) return null;
        var pic1 = photos[0];
        var pic2 = photos[1];
        var mat1 = new Mat(pic1.Path, ImreadModes.AnyDepth);
        var mat2 = new Mat(pic2.Path, ImreadModes.AnyDepth);
        var mat = new Mat();
        if (pic1.Type == 1) mat = OpenCvUtils.Multiply(mat2, mat1);

        if (pic2.Type == 1) mat = OpenCvUtils.Multiply(mat1, mat2);

        var picName = $"C_{DateTime.Now:yyyyMMddHHmmss}";

        // 保存图片
        var path = Path.Combine(FileUtils.Exposure, $"{picName}.png");
        mat.SaveImage(path);

        // 保存缩略图
        var thumb = new Mat();
        Cv2.ConvertScaleAbs(mat, thumb, 255 / 65535.0);
        Cv2.Resize(thumb, thumb, new Size(500, 500));
        var thumbnail = Path.Combine(FileUtils.Thumbnail, $"{picName}.jpg");
        thumb.SaveImage(thumbnail);

        return await AddReturnModel(new Photo
        {
            AlbumId = pic1.AlbumId,
            Name = picName,
            Path = path,
            Width = mat.Width,
            Height = mat.Height,
            Type = 2,
            Thumbnail = thumbnail
        });
    }

    #endregion
}