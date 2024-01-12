using Exposure.Api.Contracts.Services;
using Exposure.Api.Core;
using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using Mapster;
using Microsoft.AspNetCore.Mvc;
using SqlSugar;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class UserController : ControllerBase
{
    private readonly IOperLogService _operLog;
    private readonly IUserService _user;


    public UserController(IUserService user, IOperLogService operLog)
    {
        _user = user;
        _operLog = operLog;
    }

    /// <summary>
    ///     登录
    /// </summary>
    /// <param name="dto"></param>
    /// <returns></returns>
    [HttpPost]
    [Route("Login")]
    public async Task<HttpResult> Login([FromBody] LoginDto dto)
    {
        // 记录登录日志
        _operLog.AddOperLog("登录", $"登录系统: {dto.UserName}");
        // 登录
        return await _user.LogIn(dto.UserName, dto.Password) switch
        {
            0 => HttpResult.Success("登录成功",
                _user.GetLogged()),
            1 => HttpResult.Fail("登录失败: 用户不存在", null),
            2 => HttpResult.Fail("登录失败: 密码错误", null),
            3 => HttpResult.Fail("登录失败: 用户被禁用", null),
            4 => HttpResult.Fail("登录失败: 用户已过期", null),
            _ => HttpResult.Fail("登录失败: 未知错误", null)
        };
    }

    /// <summary>
    ///     登出
    /// </summary>
    /// <returns></returns>
    [HttpGet]
    [Route("Logout")]
    public HttpResult Logout()
    {
        // 记录登出日志
        _operLog.AddOperLog("登录", $"登出系统: {_user.GetLogged()?.Name}");
        _user.LogOut();
        return HttpResult.Success();
    }

    /// <summary>
    ///     分页查询所有用户
    /// </summary>
    /// <returns></returns>
    [HttpPost]
    [Route("Page")]
    public async Task<HttpResult> Page([FromBody] UserQueryDto dto)
    {
        // 记录查询日志
        _operLog.AddOperLog("查询", $"查询所有用户：页码 = {dto.Page}，大小 = {dto.Size}");
        // 查询
        var total = new RefAsync<int>();
        var list = await _user.GetByPage(dto, total);
        return HttpResult.Success("查询成功", new PageOutDto<List<User>>
        {
            Page = dto.Page,
            Size = dto.Size,
            Total = total.Value,
            Data = list
        });
    }

    /// <summary>
    ///     添加用户
    /// </summary>
    /// <param name="dto"></param>
    /// <returns></returns>
    [HttpPost]
    public async Task<HttpResult> Add([FromBody] UserEditDto dto)
    {
        // 记录添加日志
        _operLog.AddOperLog("添加", $"添加用户: {dto.Name}");
        // 添加
        var user = dto.Adapt<User>();
        user.Sha = BCrypt.Net.BCrypt.HashPassword(dto.Password);
        user.Expire = DateTime.Parse(dto.Expire);
        user.CreateTime = DateTime.Now;
        user.UpdateTime = DateTime.Now;
        user.LastLoginTime = DateTime.Now;
        var res = await _user.Add(user);
        // 返回
        return res ? HttpResult.Success("添加成功", null) : HttpResult.Fail("添加失败", null);
    }

    /// <summary>
    ///     更新用户
    /// </summary>
    /// <param name="dto"></param>
    /// <returns></returns>
    [HttpPut]
    public async Task<HttpResult> Update([FromBody] UserEditDto dto)
    {
        // 记录更新日志
        _operLog.AddOperLog("更新", $"更新用户: {dto.Name}");
        // 更新
        var old = await _user.GetByPrimary(dto.Id);
        // 如果密码不为空，则更新密码
        if (dto.Password.Length > 0) old.Sha = BCrypt.Net.BCrypt.HashPassword(dto.Password);
        // 更新其他信息
        old.Name = dto.Name;
        old.Role = dto.Role;
        old.Enabled = dto.Enabled;
        old.Expire = DateTime.Parse(dto.Expire);
        old.UpdateTime = DateTime.Now;
        // 返回
        return await _user.Update(old) ? HttpResult.Success("更新成功", null) : HttpResult.Fail("更新失败");
    }

    /// <summary>
    ///     删除用户
    /// </summary>
    /// <param name="ids"></param>
    /// <returns></returns>
    [HttpDelete]
    public async Task<HttpResult> Delete([FromBody] object[] ids)
    {
        // 记录删除日志
        _operLog.AddOperLog("删除", $"删除用户: {string.Join(',', ids)}");
        // 删除
        return await _user.DeleteRange(ids) ? HttpResult.Success("删除成功", null) : HttpResult.Fail("删除失败");
    }
}