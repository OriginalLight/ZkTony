using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using Mapster;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Localization;
using SqlSugar;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class UserController(
    IUserService user, 
    IOperLogService operLog,
    IStringLocalizer<SharedResources> localizer) : ControllerBase
{
    #region 登录

    [HttpPost]
    [Route("Login")]
    public async Task<IActionResult> Login([FromBody] LoginDto dto)
    {
        // 登录
        switch (await user.LogIn(dto.UserName, dto.Password))
        {
            case 0:
            {
                // 记录登录日志
                operLog.AddOperLog(localizer.GetString("LogIn").Value, $"{dto.UserName}");
                return Ok(user.GetLogged());
            }
            case 1: throw new Exception(localizer.GetString("NoUser").Value);
            case 2: throw new Exception(localizer.GetString("ErrorPassword").Value);
            case 3: throw new Exception(localizer.GetString("DisabledUser").Value);
            case 4: throw new Exception(localizer.GetString("ExpiredUser").Value);
            default: throw new Exception(localizer.GetString("ErrorUnknown").Value);
        }
    }

    #endregion

    #region 登出

    [HttpGet]
    [Route("Logout")]
    public IActionResult Logout()
    {
        // 记录登出日志
        operLog.AddOperLog(localizer.GetString("LogOut").Value, $"{user.GetLogged()?.Name}");
        user.LogOut();
        return Ok();
    }

    #endregion

    #region 分页查询

    [HttpPost]
    [Route("Page")]
    public async Task<IActionResult> Page([FromBody] UserQueryDto dto)
    {
        // 查询
        var total = new RefAsync<int>();
        var list = await user.GetByPage(dto, total);
        var res = new PageOutDto<List<User>>
        {
            Total = total.Value,
            List = list
        };
        return Ok(res);
    }

    #endregion

    #region 根据ID查询

    [HttpGet]
    public async Task<IActionResult> GetByIds([FromQuery] int id)
    {
        // 查询
        var user1 = await user.GetByPrimary(id);
        return Ok(user1);
    }

    #endregion

    #region 添加用户

    [HttpPost]
    public async Task<IActionResult> Add([FromBody] UserAddDto dto)
    {
        // 添加
        var user1 = dto.Adapt<User>();
        user1.Sha = BCrypt.Net.BCrypt.HashPassword(dto.Password);
        user1.CreateTime = DateTime.Now;
        user1.UpdateTime = DateTime.Now;
        user1.LastLoginTime = DateTime.Now;
        // 验证用户名是否重复
        if (await user.GetByName(dto.Name) != null) return Problem(localizer.GetString("DuplicateUserName").Value);
        if (!await user.Add(user1)) return Problem(localizer.GetString("Add").Value + localizer.GetString("Failure").Value);
        // 记录添加日志
        operLog.AddOperLog(localizer.GetString("Add").Value, $"{localizer.GetString("User").Value}: {dto.Name}");
        return Ok();
    }

    #endregion

    #region 更新用户

    [HttpPut]
    public async Task<IActionResult> Update([FromBody] UserUpdateDto dto)
    {
        // 更新
        var old = await user.GetByPrimary(dto.Id);
        if (old == null) throw new Exception(localizer.GetString("NotFound").Value);
        switch (dto.OldPassword.Length)
        {
            // 如果密码不为空，则更新密码
            case > 0 when dto.NewPassword.Length > 0:
            {
                if (!BCrypt.Net.BCrypt.Verify(dto.OldPassword, old.Sha))
                    throw new Exception(localizer.GetString("ErrorPassword").Value);
                old.Sha = BCrypt.Net.BCrypt.HashPassword(dto.NewPassword);
                break;
            }
            // 如果旧密码为空，新密码不为空，则直接更新密码
            case 0 when dto.NewPassword.Length > 0:
                // 新旧密码相同
                old.Sha = BCrypt.Net.BCrypt.HashPassword(dto.NewPassword);
                break;
        }

        // 更新其他信息

        old.Name = dto.Name;
        old.Role = dto.Role;
        old.Enabled = dto.Enabled;
        old.UpdateTime = DateTime.Now;
        if (!await user.Update(old)) throw new Exception(localizer.GetString("Update").Value + localizer.GetString("Failure").Value);
        // 记录更新日志
        operLog.AddOperLog(localizer.GetString("Update").Value, $"{localizer.GetString("User").Value}: {dto.Name}");
        return Ok();
    }

    #endregion

    #region 删除用户

    [HttpDelete]
    public async Task<IActionResult> Delete([FromBody] object[] ids)
    {
        // 删除
        if (!await user.DeleteRange(ids)) return Problem(localizer.GetString("Delete").Value + localizer.GetString("Failure").Value);
        // 记录删除日志
        operLog.AddOperLog(localizer.GetString("Delete").Value, $"{localizer.GetString("User").Value}: {string.Join(',', ids)}");
        return Ok();
    }

    #endregion
}