using System.Linq.Expressions;
using SqlSugar;

namespace Exposure.Api.Contracts.Services;

public interface IBaseService<T> where T : class, new()
{
    #region Add

    /// <summary>
    ///     增加单条数据
    /// </summary>
    /// <param name="model">实体对象</param>
    /// <returns>操作是否成功</returns>
    public Task<bool> Add(T model);

    /// <summary>
    ///     增加多条数据
    /// </summary>
    /// <param name="list">实体集合</param>
    /// <returns>操作是否成功</returns>
    public Task<bool> AddRange(List<T> list);

    #endregion

    #region Delete

    /// <summary>
    ///     根据主键删除，并返回操作是否成功
    /// </summary>
    /// <typeparam name="S">主键的类型</typeparam>
    /// <param name="key">主键</param>
    /// <returns></returns>
    public Task<bool> Delete<S>(S key);


    /// <summary>
    ///     根据主键删除，并返回操作是否成功
    /// </summary>
    /// <typeparam name="S">主键类型</typeparam>
    /// <param name="keys">主键</param>
    /// <returns></returns>
    public Task<bool> DeleteRange<S>(params S[] keys);

    #endregion

    #region Update

    /// <summary>
    ///     根据主键更新 ，返回操作是否成功
    /// </summary>
    /// <param name="model"></param>
    /// <returns></returns>
    public Task<bool> Update(T model);


    /// <summary>
    ///     根据主键更新，返回操作是否成功
    /// </summary>
    /// <param name="list">实体集合</param>
    /// <returns></returns>
    public Task<bool> UpdateRange(List<T> list);

    #endregion

    #region Query

    /// <summary>
    ///     查询所有数据
    /// </summary>
    /// <returns></returns>
    public Task<List<T>> GetAll(bool isOrderBy = false, Expression<Func<T, object>> orderBy = null,
        OrderByType orderByType = OrderByType.Asc);

    /// <summary>
    ///     查询主键
    /// </summary>
    /// <param name="key"></param>
    /// <typeparam name="S"></typeparam>
    /// <returns></returns>
    public Task<T> GetByPrimary<S>(S key);

    #endregion
}