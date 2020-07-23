1.Math.abs(-2147483648) = -2147483648 ，因为数据溢出（integer overflow）

2.为什么数组的起始索引是0而不是1:

> This convention originated with machine-language programming, where the ad- dress of an array element would be computed by adding the index to the address of the beginning of an array. Starting indices at 1 would entail either a waste of space at the beginning of the array or a waste of time to subtract the 1.

