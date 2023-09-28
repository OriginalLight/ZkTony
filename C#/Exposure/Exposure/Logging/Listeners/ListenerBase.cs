// Copyright (c) Microsoft Corporation and Contributors
// Licensed under the MIT license.

namespace Exposure.Logging.Listeners;

public abstract class ListenerBase : IListener
{
    public ListenerBase(string name)
    {
        Host = null;
        Name = name;
    }

    public Options? Options => Host?.Options;

    public ILoggerHost? Host
    {
        get;
        set;
    }

    public string Name
    {
        get;
    }

    public abstract void HandleLogEvent(LogEvent evt);
}