namespace Exposure.Api.Contracts.Services;

public interface IAudioService
{
    /// <summary>
    ///     播放
    /// </summary>
    /// <param name="key"></param>
    void Play(string key);

    /// <summary>
    ///     播放 - 开关
    /// </summary>
    /// <param name="key"></param>
    void PlayWithSwitch(string key);

    /// <summary>
    ///     停止
    /// </summary>
    void Stop();
}