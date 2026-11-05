package com.flux.wear.ui.preview

import androidx.compose.ui.tooling.preview.Preview

/** The four watch shapes FLUX is checked on: nothing may be cut or touch the edges. */
@Preview(name = "Small Round", device = "id:wearos_small_round", showSystemUi = true, backgroundColor = 0xFF000000, showBackground = true)
@Preview(name = "Large Round", device = "id:wearos_large_round", showSystemUi = true, backgroundColor = 0xFF000000, showBackground = true)
@Preview(name = "Square", device = "id:wearos_square", showSystemUi = true, backgroundColor = 0xFF000000, showBackground = true)
@Preview(name = "Rectangular", device = "id:wearos_rect", showSystemUi = true, backgroundColor = 0xFF000000, showBackground = true)
annotation class FluxWearPreviews
