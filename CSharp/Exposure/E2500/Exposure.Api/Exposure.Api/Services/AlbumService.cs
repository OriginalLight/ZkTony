using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using Exposure.SqlSugar.Contracts;
using SqlSugar;

namespace Exposure.Api.Services;

public class AlbumService(
    IDbContext dbContext,
    IUserService user
) : BaseService<Album>(dbContext), IAlbumService
{
    private readonly IDbContext _context = dbContext;


    #region 分页查询

    public async Task<List<AlbumOutDto>> GetByPage(AlbumQueryDto dto, RefAsync<int> total)
    {
        var logged = user.GetLogged();
        if (logged == null) return [];
        var lower = await _context.db.Queryable<User>().Where(u => u.Role > logged.Role).ToListAsync();
        var users = lower.Append(logged).ToList();
        var ids = users.Select(u => u.Id).ToList();
        var albums = await _context.db.Queryable<Album>()
            .Where(p => ids.Contains(p.UserId))
            .WhereIF(!string.IsNullOrEmpty(dto.Name), p => dto.Name != null && p.Name.Contains(dto.Name))
            .WhereIF(dto.StartTime != null, p => p.CreateTime >= dto.StartTime)
            .WhereIF(dto.EndTime != null, p => p.CreateTime <= dto.EndTime!.Value.AddDays(1))
            .OrderBy(p => p.CreateTime, OrderByType.Desc)
            .ToPageListAsync(dto.Page, dto.Size, total);

        var albumOutList = new List<AlbumOutDto>();
        var albumIds = albums.Select(a => a.Id).ToArray();
        var photoList = await _context.db.Queryable<Photo>().Where(p => albumIds.Contains(p.AlbumId)).ToListAsync();
        foreach (var album in albums)
        {
            var u = users.FirstOrDefault(u => u.Id == album.UserId);
            if (u != null) u.Sha = string.Empty;
            var photos = photoList.Where(p => p.AlbumId == album.Id && p.Type != -2).ToList();
            var original = photoList.Where(p => p.AlbumId == album.Id && p.Type == -2).ToList();
            albumOutList.Add(new AlbumOutDto
            {
                Id = album.Id,
                Name = album.Name,
                User = u,
                Photos = photos,
                Original = original,
                CreateTime = album.CreateTime,
                UpdateTime = album.UpdateTime
            });
        }

        return albumOutList;
    }

    #endregion

    #region 添加并返回实体

    public async Task<Album> AddReturnModel(Album album)
    {
        return await _context.db.Insertable(album).ExecuteReturnEntityAsync();
    }

    #endregion

    #region 根据id查询

    public async Task<List<AlbumOutDto>> GetByIds(int[] ids)
    {
        var albums = await _context.db.Queryable<Album>().Where(p => ids.Contains(p.Id)).ToListAsync();
        var userIds = albums.Select(a => a.UserId).Distinct().ToArray();
        var users = await _context.db.Queryable<User>().Where(u => userIds.Contains(u.Id)).ToListAsync();
        var list = new List<AlbumOutDto>();
        var albumIds = albums.Select(a => a.Id).ToArray();
        var photoList = await _context.db.Queryable<Photo>().Where(p => albumIds.Contains(p.AlbumId)).ToListAsync();
        foreach (var album in albums)
        {
            var u = users.FirstOrDefault(u => u.Id == album.UserId);
            if (u != null) u.Sha = string.Empty;
            var photos = photoList.Where(p => p.AlbumId == album.Id && p.Type != -2).ToList();
            var original = photoList.Where(p => p.AlbumId == album.Id && p.Type == -2).ToList();
            list.Add(new AlbumOutDto
            {
                Id = album.Id,
                Name = album.Name,
                User = u,
                Photos = photos,
                Original = original,
                CreateTime = album.CreateTime,
                UpdateTime = album.UpdateTime
            });
        }

        return list;
    }

    #endregion

    #region 根据id查询

    public async Task<AlbumOutDto> GetById(int id)
    {
        var album = await _context.db.Queryable<Album>().FirstAsync(p => p.Id == id);
        var u = await _context.db.Queryable<User>().FirstAsync(u => u.Id == album.UserId);
        if (u != null) u.Sha = string.Empty;
        var photos = await _context.db.Queryable<Photo>().Where(p => p.AlbumId == album.Id && p.Type != -2)
            .ToListAsync();
        var original = await _context.db.Queryable<Photo>().Where(p => p.AlbumId == album.Id && p.Type == -2)
            .ToListAsync();
        return new AlbumOutDto
        {
            Id = album.Id,
            Name = album.Name,
            User = u,
            Photos = photos,
            Original = original,
            CreateTime = album.CreateTime,
            UpdateTime = album.UpdateTime
        };
    }

    #endregion

    #region 删除多条数据

    public async Task<bool> DeleteByIds(int[] ids)
    {
        var photos = await _context.db.Queryable<Photo>().Where(p => ids.Contains(p.AlbumId)).ToListAsync();
        foreach (var photo in photos)
        {
            if (!string.IsNullOrEmpty(photo.Path) && File.Exists(photo.Path)) File.Delete(photo.Path);

            if (!string.IsNullOrEmpty(photo.Thumbnail) && File.Exists(photo.Thumbnail)) File.Delete(photo.Thumbnail);
        }

        await _context.db.Deleteable<Photo>().Where(p => ids.Contains(p.AlbumId)).ExecuteCommandAsync();
        return await _context.db.Deleteable<Album>().In(ids).ExecuteCommandAsync() > 0;
    }

    #endregion


    #region 更新

    public async Task<bool> Update(AlbumUpdateDto model)
    {
        var album = await _context.db.Queryable<Album>().InSingleAsync(model.Id);
        if (album == null) return false;
        if (string.IsNullOrEmpty(model.Name)) return false;
        album.Name = model.Name;
        var res = await _context.db.Updateable(album).ExecuteCommandAsync();
        return res > 0;
    }

    #endregion
}