package com.flux.wear.ui.preview

import androidx.compose.ui.tooling.preview.Preview

/**
 * The watch shapes FLUX is checked on: nothing may be cut or touch the edges. The Wear OS reference
 * devices (small and large round, square, rectangular), Galaxy Watch 40 mm (396 px) and 44 mm
 * (450 px) at 330 dpi, Pixel Watch 41 mm (450 px at 320 dpi), and a large system font on the
 * smallest round and square screens.
 */
@Preview(name = "Galaxy Watch 40 mm", device = "spec:width=396px,height=396px,dpi=330,isRound=true", showSystemUi = true)
@Preview(name = "Galaxy Watch 44 mm", device = "spec:width=450px,height=450px,dpi=330,isRound=true", showSystemUi = true)
@Preview(name = "Small Round", device = "id:wearos_small_round", showSystemUi = true, backgroundColor = 0xFF000000, showBackground = true)
@Preview(name = "Large Round", device = "id:wearos_large_round", showSystemUi = true, backgroundColor = 0xFF000000, showBackground = true)
@Preview(name = "Square", device = "id:wearos_square", showSystemUi = true, backgroundColor = 0xFF000000, showBackground = true)
@Preview(name = "Rectangular", device = "id:wearos_rect", showSystemUi = true, backgroundColor = 0xFF000000, showBackground = true)
@Preview(name = "Pixel Watch 41 mm", device = "spec:width=450px,height=450px,dpi=320,isRound=true", showSystemUi = true)
@Preview(name = "Small Round · fuente grande", device = "id:wearos_small_round", fontScale = LARGE_FONT_SCALE, showSystemUi = true)
@Preview(name = "Square · fuente grande", device = "id:wearos_square", fontScale = LARGE_FONT_SCALE, showSystemUi = true)
annotation class FluxWearPreviews

/** A large system font size (1.24×): text must still fit on the smallest screens. */
const val LARGE_FONT_SCALE = 1.24f
