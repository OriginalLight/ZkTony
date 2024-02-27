using System.Linq.Expressions;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Contracts.SqlSugar;
using SqlSugar;

namespace Exposure.Api.Services;

public class BaseService<T> : IBaseService<T> where T : class, new()
{
    private readonly IDbContext context;

    public BaseService(IDbContext dbContext)
    {
        context = dbContext;
    }

    public async Task<bool> Add(T model)
    {
        return await context.db.Insertable(model).ExecuteReturnIdentityAsync() > 0;
    }

    public async Task<bool> AddRange(List<T> list)
    {
        return await context.db.Insertable(list).ExecuteReturnIdentityAsync() > 0;
    }

    public async Task<bool> Delete<S>(S key)
    {
        return await context.db.Deleteable<T>().In(key).ExecuteCommandAsync() > 0;
    }

    public async Task<bool> DeleteRange<S>(params S[] keys)
    {
        return await context.db.Deleteable<T>().In(keys).ExecuteCommandAsync() > 0;
    }

    public async Task<bool> Update(T model)
    {
        return await context.db.Updateable(model).ExecuteCommandAsync() > 0;
    }

    public async Task<bool> UpdateRange(List<T> list)
    {
        return await context.db.Updateable(list).ExecuteCommandAsync() > 0;
    }

    public async Task<List<T>> GetAll(bool isOrderBy = false, Expression<Func<T, object>>? orderBy = null,
        OrderByType orderByType = OrderByType.Asc)
    {
        return await context.db.Queryable<T>().OrderByIF(isOrderBy, orderBy, orderByType).ToListAsync();
    }

    public async Task<T?> GetByPrimary<S>(S key)
    {
        return await context.db.Queryable<T>().In(key).SingleAsync();
    }
}