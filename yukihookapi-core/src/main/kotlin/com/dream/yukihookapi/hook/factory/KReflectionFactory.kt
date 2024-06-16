@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE", "NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
package com.dream.yukihookapi.hook.factory

import com.dream.yukihookapi.hook.KYukiHookAPI
import com.dream.yukihookapi.hook.core.KCallableHookCreator
import com.dream.yukihookapi.hook.entity.KYukiBaseHooker
import com.dream.yukihookapi.hook.param.KHookParam
import com.dream.yukihookapi.hook.param.KPackageParam
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.param.HookParam
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

/**
 * 在 [IYukiHookXposedInit] 中调用 [KYukiHookAPI.encase]
 * @param initiate Hook 方法体
 */
fun IYukiHookXposedInit.encaseKotlin(initiate: KPackageParam.() -> Unit) = KYukiHookAPI.encase(initiate)

/**
 * 在 [IYukiHookXposedInit] 中装载 [KYukiHookAPI]
 * @param hooker Hook 子类数组 - 必填不能为空
 * @throws IllegalStateException 如果 [hooker] 是空的
 */
fun IYukiHookXposedInit.encase(vararg hooker: KYukiBaseHooker) = KYukiHookAPI.encase(hooker = hooker)

/**
 * 将原版 [PackageParam] 转换为 Kotlin 拓展式 [KPackageParam]
 */
val PackageParam.kotlin get() = KPackageParam(this)

/**
 * 将原版 [YukiMemberHookCreator.MemberHookCreator] 转换为 Kotlin 拓展式 [KCallableHookCreator]
 */
val YukiMemberHookCreator.MemberHookCreator.kotlin get() = KCallableHookCreator(this)

/**
 * 将原版 [HookParam] 转换为 Kotlin 拓展式 [KHookParam]
 */
val HookParam.kotlin get() = KHookParam.create(this)

/**
 * 将原版 [YukiMemberHookCreator.MemberHookCreator.Result] 转换为 Kotlin 拓展式 [KCallableHookCreator.Result]
 */
val YukiMemberHookCreator.MemberHookCreator.Result.kotlin get() = KCallableHookCreator.Result(this)

/**
 * 将 Kotlin 拓展式 [KYukiBaseHooker] 转换为 原版 [YukiBaseHooker]
 */
val KYukiBaseHooker.yuki:YukiBaseHooker get() {
    return object : YukiBaseHooker() {
        override fun onHook() {
            this@yuki.assignInstance(this.kotlin)
        }
    }
}

/**
 * 将 Kotlin 拓展式 [KYukiBaseHooker] 转换为 原版 [YukiBaseHooker]
 */
val Array<out KYukiBaseHooker>.yuki:Array<out YukiBaseHooker> get() = this.map { it.yuki }.toTypedArray()