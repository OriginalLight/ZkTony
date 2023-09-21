// Copyright (c) Microsoft Corporation and Contributors
// Licensed under the MIT license.

namespace Exposure.Logging;

public class GlobalLog
{
    public static Logger? Logger { get; } = new ComponentLogger("App Log").Logger;
}
