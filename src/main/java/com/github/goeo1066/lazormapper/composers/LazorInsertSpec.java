package com.github.goeo1066.lazormapper.composers;

import java.util.List;

public record LazorInsertSpec<S>(
        List<S> entityList
) {
}
