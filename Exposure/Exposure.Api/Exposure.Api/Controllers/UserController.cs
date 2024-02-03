using Exposure.Api.Contracts.Services;
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
    public async Task<IActionResult> Login([FromBody] LoginDto dto)
    {
        // 登录
        switch (await _user.LogIn(dto.UserName, dto.Password))
        {
            case 0:
            {
                // 记录登录日志
                _operLog.AddOperLog("登录", $"登录系统: {dto.UserName}");
                return new JsonResult(_user.GetLogged());
            }
            case 1: return Problem("用户不存在");
            case 2: return Problem("密码错误");
            case 3: return Problem("用户被禁用");
            case 4: return Problem("用户已过期");
            default: return Problem("未知错误");
        }
    }

    /// <summary>
    ///     登出
    /// </summary>
    /// <returns></returns>
    [HttpGet]
    [Route("Logout")]
    public IActionResult Logout()
    {
        // 记录登出日志
        _operLog.AddOperLog("注销", $"登出系统: {_user.GetLogged()?.Name}");
        _user.LogOut();
        return Ok();
    }

    /// <summary>
    ///     分页查询所有用户
    /// </summary>
    /// <returns></returns>
    [HttpPost]
    [Route("Page")]
    public async Task<IActionResult> Page([FromBody] UserQueryDto dto)
    {
        // 查询
        var total = new RefAsync<int>();
        var list = await _user.GetByPage(dto, total);
        return new JsonResult(new PageOutDto<List<User>>
        {
            Total = total.Value,
            List = list
        });
    }
    
    [HttpGet]
    public async Task<IActionResult> GetByIds([FromQuery] int id)
    {
        // 查询
        var user = await _user.GetByPrimary(id);
        return Ok(user);
    }

    /// <summary>
    ///     添加用户
    /// </summary>
    /// <param name="dto"></param>
    /// <returns></returns>
    [HttpPost]
    public async Task<IActionResult> Add([FromBody] UserAddDto dto)
    {
        // 添加
        var user = dto.Adapt<User>();
        user.Sha = BCrypt.Net.BCrypt.HashPassword(dto.Password);
        user.CreateTime = DateTime.Now;
        user.UpdateTime = DateTime.Now;
        user.LastLoginTime = DateTime.Now;
        // 验证用户名是否重复
        if (await _user.GetByName(dto.Name) != null) return Problem("用户名重复");
        if (await _user.Add(user))
        {
            // 记录添加日志
            _operLog.AddOperLog("添加", $"添加用户: {dto.Name}");
            return Ok("添加成功");
        }

        // 返回
        return Problem("添加失败");
    }

    /// <summary>
    ///     更新用户
    /// </summary>
    /// <param name="dto"></param>
    /// <returns></returns>
    [HttpPut]
    public async Task<IActionResult> Update([FromBody] UserUpdateDto dto)
    {
        // 更新
        var old = await _user.GetByPrimary(dto.Id);
        // 如果密码不为空，则更新密码
        if (dto.OldPassword.Length > 0 && dto.NewPassword.Length > 0)
        {
            if (!BCrypt.Net.BCrypt.Verify(dto.OldPassword, old.Sha))
                return Problem("旧密码错误");
            old.Sha = BCrypt.Net.BCrypt.HashPassword(dto.NewPassword);
        }

        // 如果旧密码为空，新密码不为空，则直接更新密码
        if (dto.OldPassword.Length == 0 && dto.NewPassword.Length > 0)
            old.Sha = BCrypt.Net.BCrypt.HashPassword(dto.NewPassword);
        // 更新其他信息
        old.Name = dto.Name;
        old.Role = dto.Role;
        old.Enabled = dto.Enabled;
        old.UpdateTime = DateTime.Now;
        if (await _user.Update(old))
        {
            // 记录更新日志
            _operLog.AddOperLog("更新", $"更新用户: {dto.Name}");
            return Ok("更新成功");
        }

        return Problem("更新失败");
    }

    /// <summary>
    ///     删除用户
    /// </summary>
    /// <param name="ids"></param>
    /// <returns></returns>
    [HttpDelete]
    public async Task<IActionResult> Delete([FromBody] object[] ids)
    {
        // 删除
        if (await _user.DeleteRange(ids))
        {
            // 记录删除日志
            _operLog.AddOperLog("删除", $"删除用户: {string.Join(',', ids)}");
            return Ok("删除成功");
        }

        return Problem("删除失败");
    }
}