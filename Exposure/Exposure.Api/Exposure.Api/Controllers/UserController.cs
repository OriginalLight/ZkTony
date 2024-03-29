using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using Mapster;
using Microsoft.AspNetCore.Mvc;
using SqlSugar;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class UserController(ILogger<UserController> logger, IUserService user, IOperLogService operLog) : ControllerBase
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
                operLog.AddOperLog("登录", $"登录系统: {dto.UserName}");
                logger.LogInformation("登录成功");
                return Ok(user.GetLogged());
            }
            case 1: throw new Exception("用户不存在");
            case 2: throw new Exception("密码错误");
            case 3: throw new Exception("用户被禁用");
            case 4: throw new Exception("用户已过期");
            default: throw new Exception("未知错误");
        }
    }

    #endregion

    #region 登出

    [HttpGet]
    [Route("Logout")]
    public IActionResult Logout()
    {
        // 记录登出日志
        operLog.AddOperLog("注销", $"登出系统: {user.GetLogged()?.Name}");
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
        logger.LogInformation("分页查询成功");
        return Ok(res);
    }

    #endregion

    #region 根据ID查询

    [HttpGet]
    public async Task<IActionResult> GetByIds([FromQuery] int id)
    {
        // 查询
        var user1 = await user.GetByPrimary(id);
        logger.LogInformation("查询成功");
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
        if (await user.GetByName(dto.Name) != null) return Problem("用户名重复");
        if (!await user.Add(user1)) return Problem("添加失败");
        // 记录添加日志
        operLog.AddOperLog("添加", $"添加用户: {dto.Name}");
        logger.LogInformation("添加成功");
        return Ok("添加成功");
    }

    #endregion

    #region 更新用户

    [HttpPut]
    public async Task<IActionResult> Update([FromBody] UserUpdateDto dto)
    {
        // 更新
        var old = await user.GetByPrimary(dto.Id);
        if (old == null) throw new Exception("更新失败");
        switch (dto.OldPassword.Length)
        {
            // 如果密码不为空，则更新密码
            case > 0 when dto.NewPassword.Length > 0:
            {
                if (!BCrypt.Net.BCrypt.Verify(dto.OldPassword, old.Sha))
                    throw new Exception("旧密码错误");
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
        if (!await user.Update(old)) throw new Exception("更新失败");
        // 记录更新日志
        operLog.AddOperLog("更新", $"更新用户: {dto.Name}");
        logger.LogInformation("更新成功");
        return Ok("更新成功");
    }

    #endregion

    #region 删除用户

    [HttpDelete]
    public async Task<IActionResult> Delete([FromBody] object[] ids)
    {
        // 删除
        if (!await user.DeleteRange(ids)) return Problem("删除失败");
        // 记录删除日志
        operLog.AddOperLog("删除", $"删除用户: {string.Join(',', ids)}");
        logger.LogInformation("删除成功");
        return Ok("删除成功");
    }

    #endregion
}