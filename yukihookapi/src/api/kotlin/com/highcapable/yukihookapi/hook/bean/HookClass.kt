/*
 * YukiHookAPI - An efficient Kotlin version of the Xposed Hook API.
 * Copyright (C) 2019-2022 HighCapable
 * https://github.com/fankes/YukiHookAPI
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * This file is Created by fankes on 2022/2/9.
 */
package com.highcapable.yukihookapi.hook.bean

/**
 * 创建一个当前 Hook 的 [Class] 接管类
 * @param instance 实例
 * @param name 完整名称
 * @param throwable 异常
 */
class HookClass internal constructor(
    @PublishedApi internal var instance: Class<*>? = null,
    @PublishedApi internal var name: String,
    @PublishedApi internal var throwable: Throwable? = null
) {

    override fun toString() = "[class] $name [throwable] $throwable [instance] $instance"
}