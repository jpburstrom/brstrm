//Number with space
+ PathName {
    nextName2 {
        var nextName;
        nextName = if (fullPath.last.isDecDigit,
            { this.noEndNumbers ++ (this.endNumber + 1).asString;
            },
            {fullPath ++ " 1"; }
        );
        ^nextName;
    }

}