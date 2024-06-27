using Exposure.Api.Models.Dto;

namespace Exposure.Api.Contracts.Services;

public interface ITestService
{
    void AgingTest(TestAgingDto dto);
}