using Exposure.Api.Contracts.Services;
using NAudio.Wave;
using Serilog;

namespace Exposure.Api.Services;

public class AudioService(IOptionService option) : IAudioService
{
    #region 播放

    public void Play(string key)
    {
        try
        {
            var file = new AudioFileReader(key);
            var output = new WaveOutEvent();
            output.Init(file);
            output.Play();
        }
        catch (Exception e)
        {
            Console.WriteLine(e);
        }
    }

    #endregion

    #region 播放 - 开关

    public void PlayWithSwitch(string key)
    {
        try
        {
            switch (option.GetOptionValue("Sound"))
            {
                case null or "0":
                    return;
                case "1":
                    key = "Assets/Ringtones/" + key + ".mp3";
                    break;
                default:
                    key = "Assets/Voices/" + key + ".mp3";
                    break;
            }

            var file = new AudioFileReader(key);
            var output = new WaveOutEvent();
            output.Init(file);
            output.Play();
        }
        catch (Exception e)
        {
            Log.Error(e, e.Message);
        }
    }

    #endregion

    #region 停止

    public void Stop()
    {
    }

    #endregion
}