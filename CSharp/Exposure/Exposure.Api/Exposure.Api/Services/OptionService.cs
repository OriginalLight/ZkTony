using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;
using Exposure.SqlSugar.Contracts;

namespace Exposure.Api.Services;

public class OptionService(IDbContext dbContext) : BaseService<Option>(dbContext), IOptionService
{
    private readonly IDbContext _context = dbContext;
    // Key-Value Cache
    private readonly Dictionary<string, string> _options = new();

    #region 获取Key对应的值

    public async Task<string?> GetOptionValueAsync(string key)
    {
        if (_options.TryGetValue(key, out var value))
        {
            return value;
        }

        value = await _context.db.Queryable<Option>().Where(p => p.Key == key).Select(p => p.Value).FirstAsync();
        _options[key] = value;
        return value;
    }

    #endregion

    #region 设置Key对应的值

    public async Task<bool> SetOptionValueAsync(string key, string value)
    {
        var option = await _context.db.Queryable<Option>().FirstAsync(p => p.Key == key);
        if (option != null)
        {
            option.Value = value;
            _options[key] = value;
            return await _context.db.Updateable(option).ExecuteCommandAsync() > 0;
        }

        option = new Option
        {
            Key = key,
            Value = value
        };
        _options[key] = value;
        return await _context.db.Insertable(option).ExecuteCommandAsync() > 0;
    }

    #endregion

    #region 获取Key对应的值

    public string? GetOptionValue(string key)
    {
        if (_options.TryGetValue(key, out var value))
        {
            return value;
        }
        
        value = _context.db.Queryable<Option>().Where(p => p.Key == key).Select(p => p.Value).First();
        _options[key] = value;
        return value;
    }

    #endregion

    #region 设置Key对应的值

    public bool SetOptionValue(string key, string value)
    {
        var option = _context.db.Queryable<Option>().First(p => p.Key == key);
        if (option != null)
        {
            option.Value = value;
            _options[key] = value;
            return _context.db.Updateable(option).ExecuteCommand() > 0;
        }

        option = new Option
        {
            Key = key,
            Value = value
        };
        _options[key] = value;
        return _context.db.Insertable(option).ExecuteCommand() > 0;
    }

    #endregion

    #region 获取所有

    public async Task<IDictionary<string, string>> GetAllAsync()
    {
        var ops = await _context.db.Queryable<Option>().ToListAsync();
        var dict = ops.ToDictionary(p => p.Key, p => p.Value);
        
        // Clear and re-add
        _options.Clear();
        foreach (var (key, value) in dict)
        {
            _options[key] = value;
        }

        return dict;
    }

    #endregion
}