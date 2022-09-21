# 宿主资源注入扩展

> 这是一个将模块资源、`Activity` 组件以及 `Context` 主题注入到宿主的扩展功能。

在使用以下功能之前，为防止资源 ID 互相冲突，你需要在当前 Xposed 模块项目的 `build.gradle` 中修改资源 ID。

- Kotlin Gradle DSL

```kotlin
android {
    androidResources.additionalParameters("--allow-reserved-package-id", "--package-id", "0x64")
}
```

- Groovy

```groovy
android {
    aaptOptions.additionalParameters '--allow-reserved-package-id', '--package-id', '0x64'
}
```

::: warning

提供的示例资源 ID 值仅供参考，不可使用 **0x7f**，默认为 **0x64**，为了防止当前宿主存在多个 Xposed 模块，建议自定义你自己的资源 ID。

:::

## 注入模块资源 (Resources)

在 Hook 宿主之后，我们可以直接在 Hooker 中得到的 `Context` 注入当前模块资源。

> 示例如下

```kotlin
injectMember {
    method {
        name = "onCreate"
        param(BundleClass)
    }
    afterHook {
        instance<Activity>().also {
            // <方案1> 通过 Context 注入模块资源
            it.injectModuleAppResources()
            // <方案2> 直接得到宿主 Resources 注入模块资源
            it.resources.injectModuleAppResources()
            // 直接使用模块资源 ID
            it.getString(R.id.app_name)
        }
    }
}
```

你还可以直接在 `AppLifecycle` 中注入当前模块资源。

> 示例如下

```kotlin
onAppLifecycle {
    onCreate {
        // 全局注入模块资源，但仅限于全局生命周期
        // 类似 ImageView.setImageResource 这样的方法在 Activity 中需要单独注入
        // <方案1> 通过 Context 注入模块资源
        injectModuleAppResources()
        // <方案2> 直接得到宿主 Resources 注入模块资源
        resources.injectModuleAppResources()
        // 直接使用模块资源 ID
        getString(R.id.app_name)
    }
}
```

::: tip

更多功能请参考 [Context+Resources.injectModuleAppResources](../public/com/highcapable/yukihookapi/hook/factory/YukiHookFactory#context-resources-injectmoduleappresources-ext-method) 方法。

:::

## 注册模块 Activity

在 Android 系统中所有应用的 `Activity` 启动时，都需要在 `AndroidManifest.xml` 中进行注册，在 Hook 过程中，如果我们想通过宿主来直接启动模块中未注册的 `Activity` 要怎么做呢？

在 Hook 宿主之后，我们可以直接在 Hooker 中得到的 `Context` 注册当前模块的 `Activity` 代理。

> 示例如下

```kotlin
injectMember {
    method {
        name = "onCreate"
        param(BundleClass)
    }
    afterHook {
        instance<Activity>().registerModuleAppActivities()
    }
}
```

你还可以直接在 `AppLifecycle` 中注册当前模块的 `Activity` 代理。

> 示例如下

```kotlin
onAppLifecycle {
    onCreate {
        registerModuleAppActivities()
    }
}
```

如果没有填写 `proxy` 参数，API 将会根据当前 `Context` 自动获取当前宿主的启动入口 `Activity` 进行代理。

通常情况下，它是有效的，但是以上情况在一些 APP 中会失效，例如一些 `Activity` 会在注册清单上加入启动参数，那么我们就需要使用另一种解决方案。

若未注册的 `Activity` 不能被正确启动，我们可以手动拿到宿主的 `AndroidManifest.xml` 进行分析，来得到一个注册过的 `Activity` 标签，获取其中的 `name`。

你需要选择一个当前宿主可能用不到的、不需要的 `Activity` 作为一个“傀儡”将其进行代理，通常是有效的。

比如我们已经找到了能够被代理的合适 `Activity`。

> 示例如下

```xml
<activity
    android:name="com.demo.test.activity.TestActivity"
    ...>
```

根据其中的 `name`，我们只需要在方法中加入这个参数进行注册即可。

> 示例如下

```kotlin
registerModuleAppActivities(proxy = "com.demo.test.activity.TestActivity")
```

另一种情况，如果你对宿主的类编写了一个 `stub`，那么你可以直接通过 `Class` 对象来进行注册。

> 示例如下

```kotlin
registerModuleAppActivities(TestActivity::class.java)
```

注册完成后，请将你需要使用宿主启动的模块中的 `Activity` 继承于 `ModuleAppActivity` 或 `ModuleAppCompatActivity`。

这些 `Activity` 现在无需注册即可无缝存活于宿主中。

> 示例如下

```kotlin
class HostTestActivity : ModuleAppActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 模块资源已被自动注入，可以直接使用 xml 装载布局
        setContentView(R.layout.activity_main)
    }
}
```

若你需要继承于 `ModuleAppCompatActivity`，你需要手动设置 AppCompat 主题。

> 示例如下

```kotlin
class HostTestActivity : ModuleAppCompatActivity() {

    // 这里的主题名称仅供参考，请填写你模块中已有的主题名称
    override val moduleTheme get() = R.style.Theme_AppCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 模块资源已被自动注入，可以直接使用 xml 装载布局
        setContentView(R.layout.activity_main)
    }
}
```

以上步骤全部完成后，你就可以在 (Xposed) 宿主环境任意存在 `Context` 的地方愉快地调用 `startActivity` 了。

> 示例如下

```kotlin
val context: Context = ... // 假设这就是你的 Context
context.startActivity(context, HostTestActivity::class.java)
```

::: tip

更多功能请参考 [Context.registerModuleAppActivities](../public/com/highcapable/yukihookapi/hook/factory/YukiHookFactory#context-registermoduleappactivities-ext-method) 方法。

:::

## 创建 ContextThemeWrapper 代理

有时候，我们需要使用 `MaterialAlertDialogBuilder` 来美化自己在宿主中的对话框，但是拿不到 AppCompat 主题就无法创建。

- 会得到如下异常

```:no-line-numbers
The style on this component requires your app theme to be Theme.AppCompat (or a descendant).
```

这时，我们想在宿主被 Hook 的当前 `Activity` 中使用 `MaterialAlertDialogBuilder` 来创建对话框，就可以有如下方法。

> 示例如下

```kotlin
injectMember {
    method {
        name = "onCreate"
        param(BundleClass)
    }
    afterHook {
        // 使用 applyModuleTheme 创建一个当前模块中的主题资源
        val appCompatContext = instance<Activity>().applyModuleTheme(R.style.Theme_AppCompat)
        // 直接使用这个包装了模块主题后的 Context 创建对话框
        MaterialAlertDialogBuilder(appCompatContext)
            .setTitle("AppCompat 主题对话框")
            .setMessage("我是一个在宿主中显示的 AppCompat 主题对话框。")
            .setPositiveButton("确定", null)
            .show()
    }
}
```

你还可以对当前 `Context` 通过 `uiMode` 设置原生的夜间模式和日间模式，至少需要 Android 10 及以上系统版本支持且当前主题包含夜间模式相关元素。

> 示例如下

```kotlin
injectMember {
    method {
        name = "onCreate"
        param(BundleClass)
    }
    afterHook {
        // 定义当前模块中的主题资源
        var appCompatContext: ModuleContextThemeWrapper
        // <方案1> 直接得到 Configuration 对象进行设置
        appCompatContext = instance<Activity>()
            .applyModuleTheme(R.style.Theme_AppCompat)
            .applyConfiguration { uiMode = Configuration.UI_MODE_NIGHT_YES }
        // <方案2> 创建一个新的 Configuration 对象
        // 此方案会破坏当前宿主中原有的字体缩放大小等设置，你需要手动重新传递 densityDpi 等参数
        appCompatContext = instance<Activity>().applyModuleTheme(
            theme = R.style.Theme_AppCompat,
            configuration = Configuration().apply { uiMode = Configuration.UI_MODE_NIGHT_YES }
        )
        // 直接使用这个包装了模块主题后的 Context 创建对话框
        MaterialAlertDialogBuilder(appCompatContext)
            .setTitle("AppCompat 主题对话框")
            .setMessage("我是一个在宿主中显示的 AppCompat 主题对话框。")
            .setPositiveButton("确定", null)
            .show()
    }
}
```

这样，我们就可以在宿主中非常简单地使用 `MaterialAlertDialogBuilder` 创建对话框了。

::: warning 可能存在的问题

由于一些 APP 自身使用的 **androidx** 依赖库或自定义主题可能会对当前 **MaterialAlertDialog** 实际样式造成干扰，例如对话框的按钮样式，这种情况你可以参考 **模块 Demo** 中 [这里的示例代码](https://github.com/fankes/YukiHookAPI/tree/master/demo-module/src/main/java/com/highcapable/yukihookapi/demo_module/hook/factory/ComponentCompatFactory.kt) 来修复这个问题。

某些 APP 在创建时可能会发生 **ClassCastException** 异常，请手动指定新的 **Configuration** 实例来进行修复。

:::

::: tip

更多功能请参考 [Context.applyModuleTheme](../public/com/highcapable/yukihookapi/hook/factory/YukiHookFactory#context-applymoduletheme-ext-method) 方法。

:::