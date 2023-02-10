package watermark

import java.awt.Color
import java.awt.Transparency
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.system.exitProcess

fun main() {
    println("Input the image filename:")
    val fileName = File(readln())
    if (!fileName.exists()) {
        println("The file ${fileName.path} doesn't exist.")
        exitProcess(0)
    }
    val image: BufferedImage = ImageIO.read(fileName)
    if (image.colorModel.numColorComponents != 3) {
        println("The number of image color components isn't 3.")
        exitProcess(0)
    }
    if (image.colorModel.pixelSize !in listOf(24, 32)) {
        println("The image isn't 24 or 32-bit.")
        exitProcess(0)
    }

    println("Input the watermark image filename:")
    val watermarkName = File(readln())
    if (!watermarkName.exists()) {
        println("The file ${watermarkName.path} doesn't exist.")
        exitProcess(0)
    }
    val watermark: BufferedImage = ImageIO.read(watermarkName)
    if (watermark.colorModel.numColorComponents != 3) {
        println("The number of watermark color components isn't 3.")
        exitProcess(0)
    }
    if (watermark.colorModel.pixelSize !in listOf(24, 32)) {
        println("The watermark isn't 24 or 32-bit.")
        exitProcess(0)
    }
    if (!(image.width >= watermark.width && image.height >= watermark.height)) {
        println("The watermark's dimensions are larger.")
        exitProcess(0)
    }

    var useAlphaChannel = false
    var transparencyColor = Color(0, 0, 0)
    var useTransparencyColor = false
    if (watermark.colorModel.transparency == Transparency.TRANSLUCENT) {
        println("Do you want to use the watermark's Alpha channel?")
        if (readln().lowercase() == "yes") useAlphaChannel = true
    } else {
        println("Do you want to set a transparency color?")
        if (readln().lowercase() == "yes") {
            println("Input a transparency color ([Red] [Green] [Blue]):")
            val input = readln().lowercase()
            val regex = Regex("\\d[0-5]?[0-5]? \\d[0-5]?[0-5]? \\d[0-5]?[0-5]?")
            if (!(regex.matches(input))) {
                println("The transparency color input is invalid.")
                exitProcess(0)
            }
            val inputColor = input.split(' ').map { it.toInt() }
            transparencyColor = Color(
                inputColor[0],
                inputColor[1],
                inputColor[2]
            )
            useTransparencyColor = true
        }
    }

    println("Input the watermark transparency percentage (Integer 0-100):")
    val weight: Int
    try {
        weight = readln().toInt()
    } catch (e: Exception) {
        println("The transparency percentage isn't an integer number.")
        exitProcess(0)
    }
    if (weight !in 0..100) {
        println("The transparency percentage is out of range.")
        exitProcess(0)
    }

    println("Choose the position method (single, grid):")
    val positionMethod = readln().lowercase()
    if (!(Regex("single|grid").matches(positionMethod))) {
        println("The position method input is invalid.")
        exitProcess(0)
    }


    var xWaterMark = 0
    var yWaterMark = 0
    if (positionMethod == "single") {
        val diffX = image.width - watermark.width
        val diffY = image.height - watermark.height
        println("Input the watermark position ([x 0-$diffX] [y 0-$diffY]):")
        val input: List<Int>
        try {
            input = readln().split(' ').map { it.toInt() }
        } catch (e: Exception) {
            println("The position input is invalid.")
            exitProcess(0)
        }
        if (!(input[0] in 0..diffX && input[1] in 0..diffY)) {
            println("The position input is out of range.")
            exitProcess(0)
        }
        xWaterMark = input[0]
        yWaterMark = input[1]
    }


    println("Input the output image filename (jpg or png extension):")
    val outputFile = File(readln())
    if (outputFile.extension !in listOf("jpg", "png")) {
        println("The output file extension isn't \"jpg\" or \"png\".")
        exitProcess(0)
    }

    val output = BufferedImage(
        image.width, image.height,
        if (useAlphaChannel || useTransparencyColor) BufferedImage.TYPE_INT_ARGB else BufferedImage.TYPE_INT_RGB
    )
    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            output.setRGB(x, y , image.getRGB(x, y))
        }
    }

    if (positionMethod == "single") {
        setWaterMark(
            image, watermark, output, useAlphaChannel, useTransparencyColor, transparencyColor, weight,
            xWaterMark, yWaterMark
        )
    } else {
        for (x in 0 until image.width step watermark.width) {
            for (y in 0 until image.height step watermark.height) {
                setWaterMark(
                    image, watermark, output, useAlphaChannel, useTransparencyColor, transparencyColor, weight,
                    x, y
                )
            }
        }
    }

    ImageIO.write(output, outputFile.extension, outputFile)
    println("The watermarked image ${outputFile.path} has been created.")
}

fun setWaterMark(
    image: BufferedImage, watermark: BufferedImage, output: BufferedImage,
    useAlphaChannel: Boolean, useTransparencyColor: Boolean, transparencyColor: Color,
    weight: Int, x0: Int, y0: Int
) {
    val width = if (image.width - x0 < watermark.width) image.width % watermark.width else watermark.width
    val height = if (image.height - x0 < watermark.height) image.height % watermark.height else watermark.height

    for (x in 0 until width) {
        for (y in 0 until height) {
            val i = Color(image.getRGB(x + x0, y + y0))
            val w = Color(watermark.getRGB(x, y), useAlphaChannel)
            val color = if (w.alpha == 0 || (useTransparencyColor && w.rgb == transparencyColor.rgb)) i
            else Color(
                (weight * w.red + (100 - weight) * i.red) / 100,
                (weight * w.green + (100 - weight) * i.green) / 100,
                (weight * w.blue + (100 - weight) * i.blue) / 100
            )
            output.setRGB(x + x0, y + y0, color.rgb)
        }
    }
}