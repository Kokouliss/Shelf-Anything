package com.kokouliss.shelfanything.core.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.kokouliss.shelfanything.core.ui.R

/** Material Symbols (outlined) shipped as vector drawables in this module. */
object AppIcons {
    val Add = R.drawable.core_ui_ic_add
    val ArrowBack = R.drawable.core_ui_ic_arrow_back
    val Barcode = R.drawable.core_ui_ic_qr_code_scanner
    val Check = R.drawable.core_ui_ic_check
    val Close = R.drawable.core_ui_ic_close
    val ContentCopy = R.drawable.core_ui_ic_content_copy
    val Delete = R.drawable.core_ui_ic_delete
    val Edit = R.drawable.core_ui_ic_edit
    val Logout = R.drawable.core_ui_ic_logout
    val Mail = R.drawable.core_ui_ic_mail
    val MoreVert = R.drawable.core_ui_ic_more_vert
    val OpenInNew = R.drawable.core_ui_ic_open_in_new
    val Refresh = R.drawable.core_ui_ic_refresh
    val Search = R.drawable.core_ui_ic_search
    val Settings = R.drawable.core_ui_ic_settings
    val Share = R.drawable.core_ui_ic_share
}

/** Pass null as [contentDescription] only when the icon sits next to text that says the same. */
@Composable
fun AppIcon(
    @DrawableRes icon: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    Icon(painter = painterResource(icon), contentDescription = contentDescription, modifier = modifier, tint = tint)
}
