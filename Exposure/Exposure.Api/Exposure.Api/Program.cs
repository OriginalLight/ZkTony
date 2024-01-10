using Exposure.Api.Contracts.Repositories;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Contracts.SqlSugar;
using Exposure.Api.Core;
using Exposure.Api.Repositories;
using Exposure.Api.Services;
using Exposure.Api.SqlSugar;
using Microsoft.OpenApi.Models;

var builder = WebApplication.CreateBuilder(args);

// Loggers
builder.Logging.ClearProviders();
builder.Logging.AddConsole();
builder.Logging.AddDebug();

// Add services to the container.
builder.Services.AddControllers().AddJsonOptions(options =>
{
    options.JsonSerializerOptions.Converters.Add(new DateTimeJsonConverter());
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

// Add DbContext
builder.Services.AddTransient<IDbContext, AppDbContext>();

// Add Services
builder.Services.AddHostedService<ApplicationHostService>();
builder.Services.AddSingleton<ISerialPortService, SerialPortService>();
builder.Services.AddSingleton<IUsbService, UsbService>();
builder.Services.AddSingleton<IUserService, UserService>();
builder.Services.AddSingleton<ICameraService, CameraService>();
builder.Services.AddSingleton<IErrorLogService, ErrorLogService>();
builder.Services.AddSingleton<IOperLogService, OperLogService>();
builder.Services.AddSingleton<IPictureService, PictureService>();

// Add Repositories
builder.Services.AddTransient(typeof(IBaseRepository<>), typeof(BaseRepository<>));
builder.Services.AddTransient<IErrorLogRepository, ErrorLogRepository>();
builder.Services.AddTransient<IOperLogRepository, OperLogRepository>();
builder.Services.AddTransient<IUserRepository, UserRepository>();
builder.Services.AddTransient<IPictureRepository, PictureRepository>();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseWebSockets();

app.UseHttpsRedirection();

app.UseAuthorization();

app.MapControllers();

app.UseMiddleware<ExceptionHandlerMiddleware>();

app.Run();