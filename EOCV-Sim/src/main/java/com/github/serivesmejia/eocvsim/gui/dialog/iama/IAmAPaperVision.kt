package com.github.serivesmejia.eocvsim.gui.dialog.iama

import com.github.serivesmejia.eocvsim.gui.DialogFactory
import com.github.serivesmejia.eocvsim.gui.Visualizer
import java.awt.BorderLayout
import java.awt.Desktop
import java.awt.Dimension
import java.awt.FlowLayout
import java.net.URI
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.SwingConstants

class IAmAPaperVision(
    parent: JFrame,
    visualizer: Visualizer,
    specificallyInterested: Boolean = false,
    showWorkspacesButton: Boolean = true
) {

    companion object {
        val papervisionGif = ImageIcon(this::class.java.getResource("/images/papervision.gif"))
    }

    val dialog = JDialog(parent).apply {
        isModal = true
        title = "Welcome !"

        layout = BoxLayout(contentPane, BoxLayout.Y_AXIS)

        size = Dimension(820, 550)
        isResizable = false
        setLocationRelativeTo(null)
    }

    init {
        val title = JLabel("<html><div style='text-align: center;'><b>Introducing PaperVision</b></div></html>")

        title.font = title.font.deriveFont(20f)
        title.horizontalAlignment = SwingConstants.CENTER
        title.alignmentX = JPanel.CENTER_ALIGNMENT // Align horizontally in the BoxLayout
        dialog.contentPane.add(title)

        dialog.contentPane.add(Box.createVerticalStrut(5))

        val gifPanel = JPanel(BorderLayout())
        val label = JLabel(papervisionGif)
        gifPanel.add(label, BorderLayout.CENTER)

        gifPanel.alignmentX = JPanel.CENTER_ALIGNMENT // Align the panel in the BoxLayout
        dialog.contentPane.add(gifPanel)

        val text = """
            <html>
                <div style='text-align: center;'>
                    PaperVision is a new pipeline development tool that allows you to create<br>
                    your OpenCV algorithms with a visual programming interface, easier than ever before.
                </div>
            </html>
        """.trimIndent()

        dialog.contentPane.add(JLabel(text).apply {
            font = font.deriveFont(18f)
            horizontalAlignment = SwingConstants.CENTER
            alignmentX = JPanel.CENTER_ALIGNMENT // Align horizontally in the BoxLayout
        })

        dialog.contentPane.add(Box.createVerticalStrut(10))

        // Create the buttons panel
        val buttonsPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)

            add(Box.createHorizontalGlue()) // Align the button to the right


            add(JButton("Close").apply {
                addActionListener {
                    // Handle the next button click here
                    dialog.dispose() // Close the dialog on click
                }
            })

            add(Box.createHorizontalStrut(10)) // Add some space between the buttons

            if(!specificallyInterested && showWorkspacesButton) {
                add(JButton("Use Workspaces Instead").apply {
                    addActionListener {
                        dialog.dispose() // Close the dialog on click
                        DialogFactory.createWorkspace(visualizer)
                    }
                })

                add(Box.createHorizontalStrut(10)) // Add some space between the buttons
            }

            add(JButton("Use PaperVision").apply {
                addActionListener {
                    dialog.dispose() // Close the dialog on click

                    val indexOfTab = visualizer.pipelineOpModeSwitchablePanel.indexOfTab("PaperVision")
                    if(indexOfTab >= 0) {
                        visualizer.pipelineOpModeSwitchablePanel.selectedIndex = indexOfTab
                    } else {
                        JOptionPane.showMessageDialog(
                            parent,
                            "PaperVision is not currently available, please check your plugin settings.",
                            "Warning",
                            JOptionPane.ERROR_MESSAGE
                        )
                        return@addActionListener
                    }

                    fun openPaperVisionByDefault() {
                        visualizer.eocvSim.config.flags["prefersPaperVision"] = true
                    }

                    if(specificallyInterested) {
                        openPaperVisionByDefault()

                        JOptionPane.showConfirmDialog(
                            parent,
                            "From now on, EOCV-Sim will focus on PaperVision upon startup.\nYou can change this in the settings.",
                            "PaperVision",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.INFORMATION_MESSAGE
                        )
                    } else {
                        JOptionPane.showOptionDialog(
                            parent,
                            "Would you like to focus on PaperVision by default?\nThis is useful if you're not interested on the other tools.\nYou can change this in the settings.",
                            "PaperVision",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            arrayOf("Yes", "No"),
                            "No"
                        ).let {
                            if(it == JOptionPane.YES_OPTION) {
                                openPaperVisionByDefault()
                            } else {
                                visualizer.eocvSim.config.flags["prefersPaperVision"] = false
                            }
                        }
                    }
                }
            })

            border = BorderFactory.createEmptyBorder(0, 10, 10, 10)
        }

        buttonsPanel.alignmentX = JPanel.CENTER_ALIGNMENT // Align the panel in the BoxLayout
        dialog.contentPane.add(buttonsPanel)


        visualizer.eocvSim.config.flags["hasShownIamPaperVision"] = true
        dialog.isVisible = true
    }

}