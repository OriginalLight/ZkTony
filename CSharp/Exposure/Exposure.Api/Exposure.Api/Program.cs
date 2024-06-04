using Exposure.Api.Contracts.Services;
using Exposure.Api.Middleware;
using Exposure.Api.Services;
using Exposure.SqlSugar;
using Exposure.SqlSugar.Contracts;
using Exposure.Utilities;
using Microsoft.OpenApi.Models;
using Serilog;

var builder = WebApplication.CreateBuilder(args);

// Loggers
builder.Host.UseSerilog((_, config) => config.ReadFrom.Configuration(builder.Configuration));

// Add services to the container.
builder.Services.AddControllers().AddJsonOptions(options =>
{
    options.JsonSerializerOptions.Converters.Add(new JsonUtils.JsonOptions());
});

// Learn more about configuring Swagger/OpenAPI at https://aka.ms/aspnetcore/swashbuckle
builder.Services.AddEndpointsApiExplorer();

// Add Swagger
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new OpenApiInfo { Title = "Exposure.Api", Version = "v1" });
});

// Add Cors
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll", corsPolicyBuilder =>
    {
        corsPolicyBuilder.AllowAnyOrigin()
            .AllowAnyMethod()
            .AllowAnyHeader();
    });
});

// Localizations
builder.Services.AddLocalization(options => options.ResourcesPath = "Resources");

// Add DbContext
builder.Services.AddTransient<IDbContext, AppDbContext>();

// Add Services
builder.Services.AddHostedService<ApplicationHostService>();
builder.Services.AddSingleton<IStorageService, StorageService>();
builder.Services.AddSingleton<ISerialPortService, SerialPortService>();
builder.Services.AddSingleton<IUsbService, UsbService>();
builder.Services.AddSingleton<IUserService, UserService>();
builder.Services.AddSingleton<ICameraService, CameraService>();
builder.Services.AddSingleton<IAlbumService, AlbumService>();
builder.Services.AddSingleton<IErrorLogService, ErrorLogService>();
builder.Services.AddSingleton<IOptionService, OptionService>();
builder.Services.AddSingleton<IOperLogService, OperLogService>();
builder.Services.AddSingleton<IPhotoService, PhotoService>();
builder.Services.AddSingleton<ITestService, TestService>();
builder.Services.AddSingleton<IAudioService, AudioService>();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();

app.UseCors();

app.UseAuthorization();

app.MapControllers();

app.UseExceptionMiddleware();

app.UseLocalizationMiddleware();

app.Run();