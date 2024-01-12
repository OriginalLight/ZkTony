using Newtonsoft.Json;
using Newtonsoft.Json.Serialization;

namespace Exposure.Api.Helpers;

public class JsonHelper
{
    public static string Serialize(object? obj)
    {
        return JsonConvert.SerializeObject(obj, Formatting.Indented, new JsonSerializerSettings
        {
            ContractResolver = new CamelCasePropertyNamesContractResolver()
        });
    }
}