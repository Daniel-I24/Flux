package com.flux.wear.ui.preview

import androidx.compose.ui.tooling.preview.Preview

/**
 * The watch shapes FLUX is checked on: nothing may be cut or touch the edges. Galaxy Watch 40 mm
 * (396 px) and 44 mm (450 px) at 330 dpi, plus the Wear OS reference devices.
 */
@Preview(name = "Galaxy Watch 40 mm", device = "spec:width=396px,height=396px,dpi=330,isRound=true", showSystemUi = true)
@Preview(name = "Galaxy Watch 44 mm", device = "spec:width=450px,height=450px,dpi=330,isRound=true", showSystemUi = true)
@Preview(name = "Small Round", device = "id:wearos_small_round", showSystemUi = true, backgroundColor = 0xFF000000, showBackground = true)
@Preview(name = "Large Round", device = "id:wearos_large_round", showSystemUi = true, backgroundColor = 0xFF000000, showBackground = true)
@Preview(name = "Square", device = "id:wearos_square", showSystemUi = true, backgroundColor = 0xFF000000, showBackground = true)
annotation class FluxWearPreviews
