"""Draws the launcher icon: a gold crescent above a black cauldron glowing green, steam rising, on the forest night.
Writes the adaptive foreground (432 px, transparent) and docs/logo.png (512 px, with the background)."""
from PIL import Image, ImageDraw, ImageFilter
import os

ROOT = os.path.join(os.path.dirname(__file__), "..")
S = 4 * 432                      # drawn at 4x, scaled down for smooth edges
k = S / 432


def P(*xy):
    return [v * k for v in xy]


def foreground():
    im = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    # green glow above the brew
    g = Image.new("RGBA", (S, S), (0, 0, 0, 0)); gd = ImageDraw.Draw(g)
    gd.ellipse(P(110, 200, 322, 290), fill=(157, 240, 180, 150)); g = g.filter(ImageFilter.GaussianBlur(26 * k)); im.alpha_composite(g)
    # moon glow and crescent
    g = Image.new("RGBA", (S, S), (0, 0, 0, 0)); gd = ImageDraw.Draw(g)
    gd.ellipse(P(178, 92, 270, 184), fill=(242, 207, 134, 90)); g = g.filter(ImageFilter.GaussianBlur(16 * k)); im.alpha_composite(g)
    m = Image.new("L", (S, S), 0); md = ImageDraw.Draw(m)
    md.ellipse(P(186, 100, 262, 176), fill=255); md.ellipse(P(204, 92, 276, 164), fill=0)
    im.paste((242, 207, 134, 255), (0, 0), m)
    d = ImageDraw.Draw(im)
    # steam
    for dx, a in ((-26, 150), (0, 190), (26, 150)):
        x = 216 + dx
        d.arc(P(x - 12, 196, x + 12, 226), 90, 270, fill=(223, 238, 228, a), width=int(6 * k))
        d.arc(P(x - 12, 168, x + 12, 198), 270, 90, fill=(223, 238, 228, a - 60), width=int(6 * k))
    # cauldron body, legs, rim and brew
    d.chord(P(132, 176, 300, 344), 0, 180, fill=(11, 15, 12, 255))
    d.rectangle(P(132, 258, 300, 262), fill=(11, 15, 12, 255))
    d.line(P(160, 330, 148, 350), fill=(11, 15, 12, 255), width=int(11 * k))
    d.line(P(272, 330, 284, 350), fill=(11, 15, 12, 255), width=int(11 * k))
    d.ellipse(P(124, 240, 308, 276), fill=(31, 42, 34, 255), outline=(217, 178, 94, 255), width=int(6 * k))
    d.ellipse(P(140, 248, 292, 268), fill=(127, 209, 154, 255))
    d.ellipse(P(176, 251, 188, 260), fill=(200, 245, 212, 255)); d.ellipse(P(236, 253, 246, 261), fill=(200, 245, 212, 255))
    return im.resize((432, 432), Image.LANCZOS)


fg = foreground()
fg.save(os.path.join(ROOT, "app/src/main/res/drawable-nodpi/ic_launcher_fg.png"))
bg = Image.new("RGBA", (432, 432), (14, 26, 20, 255))
glow = Image.new("RGBA", (432, 432), (0, 0, 0, 0)); ImageDraw.Draw(glow).ellipse((60, 20, 372, 300), fill=(33, 67, 47, 255))
bg.alpha_composite(glow.filter(ImageFilter.GaussianBlur(60))); bg.alpha_composite(fg)
mask = Image.new("L", (432, 432), 0); ImageDraw.Draw(mask).rounded_rectangle((0, 0, 431, 431), 96, fill=255)
logo = Image.new("RGBA", (432, 432), (0, 0, 0, 0)); logo.paste(bg, (0, 0), mask)
logo.resize((512, 512), Image.LANCZOS).save(os.path.join(ROOT, "docs/logo.png"))
print("icon written")
