import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.IIOException
import javax.imageio.ImageIO
import kotlin.experimental.xor

fun main() {
    while (true) {
        println("Task (hide, show, exit):")
        when (val str = readLine()) {
            "hide" -> hide()
            "show" -> show()
            "exit" -> {
                println("Bye!")
                break
            }
            else -> println("Wrong task: $str")
        }
    }
}

fun show() {
    println("Input image file:")
    val inputFileName = readLine()!!
    val image: BufferedImage

    try {
        image =
            ImageIO.read(File(inputFileName))
    } catch (e: IIOException) {
        println("Can't read input file!")
        return
    }

    val bitMask = 0x00000001
    var x = 0
    var y = 0
    var flag: Int
    val message = StringBuilder()
    var count = 0

    for (i in 0 until image.height * image.width) {
        var bit = 0
        for (j in 0..7) {
            if (x < image.width) {
                flag = image.getRGB(x, y) and bitMask // get the last digit of the pixel
                x++
            } else {
                x = 0
                y++
                flag = image.getRGB(x, y) and bitMask // get the last digit of the pixel
            }

            if (flag == 1) { // store the extracted digits into an integer as a ASCII number
                bit = bit shl 1
                bit = bit or 0x1 // 00000001
            } else
                bit = bit shl 1
        }

        if (bit == 0 && count == 0 || bit == 0 && count == 1)
            count++
        else
            if (bit == 3 && count == 2)
                break
            else
                count = 0
        message.append(bit.toChar()) // represent the ASCII number by characters
    }

    println("Password:")
    var password = readLine()!!

    if (message.length <= password.length)
        password = password.substring(0, message.length)
    else {
        password = password.repeat(message.length / password.length + 1)
        password = password.substring(0, message.length)
    }

    val bytePassword = password.toByteArray()

    val decryptedByteMessage = ByteArray(message.length)

    for (i in message.indices)
        decryptedByteMessage[i] = message[i].toByte() xor bytePassword[i]

    val decryptedMessage = StringBuilder()
    for (ch in decryptedByteMessage)
        decryptedMessage.append(ch.toChar())
    println("Message:\n${decryptedMessage}")
}

fun hide() {
    println("Input image file:")
    val inputFileName = readLine()!!
    println("Output image file:")
    val outputFileName = readLine()!!
    println("Message to hide:")
    val message = readLine()!!
    println("Password:")
    var password = readLine()!!

    val image: BufferedImage
    try {
        image =
            ImageIO.read(File(inputFileName))
    } catch (e: IIOException) {
        println("Can't read input file!")
        return
    }

    if (message.length <= password.length)
        password = password.substring(0, message.length)
    else {
        password = password.repeat(message.length / password.length + 1)
        password = password.substring(0, message.length)
    }

    val bytePassword = password.toByteArray()

    val byteMessage = message.encodeToByteArray()
    var encryptedMessage = ByteArray(byteMessage.size)

    for (i in byteMessage.indices)
        encryptedMessage[i] = byteMessage[i] xor bytePassword[i]

    val list = encryptedMessage.toMutableList()
    list.addAll(listOf(0, 0, 3))
    encryptedMessage = list.toByteArray() // bytes array with end 0, 0, 3

    if (encryptedMessage.size * 8 > image.width * image.height) {
        println("The input image is not large enough to hold this message.")
        return
    }

    val bitMask = 0x00000001 // define the mask bit used to get the digit
    var bit: Int // define a integer number to represent the ASCII number of a character
    var x = 0
    var y = 0

    for (byte in encryptedMessage) {
        bit = byte.toInt() // get the ASCII number of a character
        for (j in 0..7) {
            val flag = (bit shr 7 - j) and bitMask // get 1 digit from the character
            if (flag == 1) {
                if (x < image.width) {
                    image.setRGB(x, y, image.getRGB(x, y) or 0x00000001)
                    x++ // store the bit which is 1 into a pixel's last digit
                } else {
                    x = 0
                    y++ // store the bit which is 1 into a pixel's last digit
                    image.setRGB(x, y, image.getRGB(x, y) or 0x00000001)
                }
            } else {
                if (x < image.width) {
                    image.setRGB(x, y, image.getRGB(x, y) and -0x2)
                    x++ // store the bit which is 0 into a pixel's last digit
                } else {
                    x = 0
                    y++ // store the bit which is 0 into a pixel's last digit
                    image.setRGB(x, y, image.getRGB(x, y) and -0x2)
                }
            }
        }
    }

    ImageIO.write(image, "png", File(outputFileName))
    println("Message saved in $outputFileName image")
}