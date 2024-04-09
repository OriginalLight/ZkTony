using System.Linq.Expressions;
using Exposure.Api.Contracts.Services;
using Exposure.SqlSugar.Contracts;
using SqlSugar;

namespace Exposure.Api.Services;

public class BaseService<T>(IDbContext dbContext) : IBaseService<T>
    where T : class, new()
{
    public async Task<bool> Add(T model)
    {
        return await dbContext.db.Insertable(model).ExecuteReturnIdentityAsync() > 0;
    }

    public async Task<bool> AddRange(List<T> list)
    {
        return await dbContext.db.Insertable(list).ExecuteReturnIdentityAsync() > 0;
    }

    public async Task<bool> Delete<S>(S key)
    {
        return await dbContext.db.Deleteable<T>().In(key).ExecuteCommandAsync() > 0;
    }

    public async Task<bool> DeleteRange<S>(params S[] keys)
    {
        return await dbContext.db.Deleteable<T>().In(keys).ExecuteCommandAsync() > 0;
    }

    public async Task<bool> Update(T model)
    {
        return await dbContext.db.Updateable(model).ExecuteCommandAsync() > 0;
    }

    public async Task<bool> UpdateRange(List<T> list)
    {
        return await dbContext.db.Updateable(list).ExecuteCommandAsync() > 0;
    }

    public async Task<List<T>> GetAll(bool isOrderBy = false, Expression<Func<T, object>>? orderBy = null,
        OrderByType orderByType = OrderByType.Asc)
    {
        return await dbContext.db.Queryable<T>().OrderByIF(isOrderBy, orderBy, orderByType).ToListAsync();
    }

    public async Task<T?> GetByPrimary<S>(S key)
    {
        return await dbContext.db.Queryable<T>().In(key).SingleAsync();
    }
}