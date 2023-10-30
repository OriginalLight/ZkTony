﻿// Copyright (c) Microsoft Corporation and Contributors
// Licensed under the MIT license.

using System.Globalization;

namespace Exposure.Logging.Helpers;

public static class StringExtensions
{
    public static string ToStringInvariant<T>(this T value) => Convert.ToString(value, CultureInfo.InvariantCulture)!;

    public static string FormatInvariant(this string value, params object[] arguments) =>
        string.Format(CultureInfo.InvariantCulture, value, arguments);
}