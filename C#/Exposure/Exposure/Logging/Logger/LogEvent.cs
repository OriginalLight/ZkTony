// Copyright (c) Microsoft Corporation and Contributors
// Licensed under the MIT license.

using Exposure.Logging.Helpers;

namespace Exposure.Logging;

public class LogEvent
{
    private LogEvent(string source, string subSource, SeverityLevel severity, string message, Exception exception,
        TimeSpan elapsed)
    {
        Source = source;
        SubSource = subSource;
        Severity = severity;
        Message = message;
        Exception = exception;
        Elapsed = elapsed;
    }

    public string Source
    {
        get;
    }

    public string? SubSource
    {
        get;
    }

    public SeverityLevel Severity
    {
        get;
    }

    public string Message
    {
        get;
    }

    public Exception? Exception
    {
        get;
    }

    public TimeSpan Elapsed
    {
        get;
        private set;
    }

    public static long NoElapsedTicks => -1L;

    public static TimeSpan NoElapsed => new(NoElapsedTicks);

    public bool HasElapsed => Elapsed.Ticks >= 0;

    public string FullSourceName
    {
        get
        {
            if (SubSource != null)
            {
                return $"{Source}/{SubSource}";
            }

            return Source;
        }
    }

    internal void SetElapsed(TimeSpan elapsed) => Elapsed = elapsed;

    public static LogEvent Create(string source, string subSource, SeverityLevel severity, string message) =>
        Create(source, subSource, severity, message, null, NoElapsed);

    public static LogEvent Create(string source, string subSource, SeverityLevel severity, string message,
        Exception exception) => Create(source, subSource, severity, message, exception, NoElapsed);

    public static LogEvent Create(string source, string subSource, SeverityLevel severity, string message,
        TimeSpan elapsed) => Create(source, subSource, severity, message, null, elapsed);

    public static LogEvent Create(string source, string subSource, SeverityLevel severity, string message,
        Exception? exception, TimeSpan elapsed) => new(source, subSource, severity, message, exception!, elapsed);

    public override string ToString()
    {
        var hasException = Exception != null;

        if (hasException && HasElapsed)
        {
            return "[{0}] {1} {2} {3} {4}".FormatInvariant(FullSourceName, Severity.ToString(), Message, Exception!,
                Elapsed);
        }

        if (hasException && !HasElapsed)
        {
            return "[{0}] {1} {2} {3}".FormatInvariant(FullSourceName, Severity.ToString(), Message, Exception!);
        }

        if (!hasException && HasElapsed)
        {
            return "[{0}] {1} {2} {3}".FormatInvariant(FullSourceName, Severity.ToString(), Message, Elapsed);
        }

        return "[{0}] {1} {2}".FormatInvariant(FullSourceName, Severity.ToString(), Message);
    }
}