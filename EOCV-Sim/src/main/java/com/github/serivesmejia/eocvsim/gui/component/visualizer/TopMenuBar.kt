/*
 * Copyright (c) 2021 Sebastian Erives
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.github.serivesmejia.eocvsim.gui.component.visualizer

import com.github.serivesmejia.eocvsim.EOCVSim
import com.github.serivesmejia.eocvsim.gui.DialogFactory
import com.github.serivesmejia.eocvsim.gui.Visualizer
import com.github.serivesmejia.eocvsim.gui.dialog.Output
import com.github.serivesmejia.eocvsim.gui.dialog.PluginOutput
import com.github.serivesmejia.eocvsim.gui.util.GuiUtil
import com.github.serivesmejia.eocvsim.input.SourceType
import com.github.serivesmejia.eocvsim.pipeline.compiler.CompiledPipelineManager
import com.github.serivesmejia.eocvsim.util.FileFilters
import com.github.serivesmejia.eocvsim.util.exception.handling.CrashReport
import com.github.serivesmejia.eocvsim.workspace.util.VSCodeLauncher
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.awt.Desktop
import java.io.File
import java.net.URI
import javax.swing.JFileChooser
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

class TopMenuBar(visualizer: Visualizer, eocvSim: EOCVSim) : JMenuBar() {

    companion object {
        val docsUrl = URI("https://docs.deltacv.org/eocv-sim/")
    }

    @JvmField val mFileMenu   = JMenu("File")
    @JvmField val mWorkspMenu = JMenu("Workspace")
    @JvmField val mHelpMenu   = JMenu("Help")

    @JvmField val workspCompile = JMenuItem("Build Java Files")

    init {
        val desktop = Desktop.getDesktop()
        // FILE
        val fileNew = JMenu("New")
        mFileMenu.add(fileNew)

        val fileNewInputSourceSubmenu = JMenu("Input Source")
        fileNew.add(fileNewInputSourceSubmenu)

        //add all input source types to top bar menu
        for (type in SourceType.values()) {
            if (type == SourceType.UNKNOWN) continue //exclude unknown type

            val fileNewInputSourceItem = JMenuItem(type.coolName)

            fileNewInputSourceItem.addActionListener {
                DialogFactory.createSourceDialog(eocvSim, type)
            }

            fileNewInputSourceSubmenu.add(fileNewInputSourceItem)
        }

        val fileSaveMat = JMenuItem("Screenshot Pipeline")

        fileSaveMat.addActionListener {
            val mat = Mat()
            visualizer.viewport.pollLastFrame(mat)
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2BGR)

            GuiUtil.saveMatFileChooser(
                visualizer.frame,
                mat,
                eocvSim
            )

            mat.release()
        }
        mFileMenu.add(fileSaveMat)

        mFileMenu.addSeparator()

        if (desktop.isSupported(Desktop.Action.APP_PREFERENCES)) {
            desktop.setPreferencesHandler { DialogFactory.createConfigDialog(eocvSim) }
        }
        else {
            val editSettings = JMenuItem("Settings")
            editSettings.addActionListener { DialogFactory.createConfigDialog(eocvSim) }
            mFileMenu.add(editSettings)
        }

        val filePlugins = JMenuItem("Manage Plugins")
        filePlugins.addActionListener { eocvSim.pluginManager.appender.append(PluginOutput.SPECIAL_OPEN_MGR)}

        mFileMenu.add(filePlugins)

        mFileMenu.addSeparator()

        val fileRestart = JMenuItem("Restart")

        fileRestart.addActionListener { eocvSim.onMainUpdate.doOnce { eocvSim.restart() } }
        mFileMenu.add(fileRestart)

        add(mFileMenu)

        //WORKSPACE

        val workspSetWorkspace = JMenuItem("Select Workspace")

        workspSetWorkspace.addActionListener { DialogFactory.createWorkspace(visualizer) }
        mWorkspMenu.add(workspSetWorkspace)

        val workspClose = JMenuItem("Close Current Workspace")

        workspClose.addActionListener {
            eocvSim.onMainUpdate.doOnce {
                eocvSim.workspaceManager.workspaceFile = CompiledPipelineManager.DEF_WORKSPACE_FOLDER
            }
        }
        mWorkspMenu.add(workspClose)

        mWorkspMenu.addSeparator()

        workspCompile.addActionListener { visualizer.asyncCompilePipelines() }
        mWorkspMenu.add(workspCompile)

        val workspBuildOutput = JMenuItem("Output")

        workspBuildOutput.addActionListener {
            if(!Output.isAlreadyOpened)
                DialogFactory.createOutput(eocvSim, true)
        }
        mWorkspMenu.add(workspBuildOutput)

        mWorkspMenu.addSeparator()

        val workspVSCode = JMenu("External")

        val workspVSCodeCreate = JMenuItem("Create Gradle Workspace")

        workspVSCodeCreate.addActionListener { visualizer.createVSCodeWorkspace() }
        workspVSCode.add(workspVSCodeCreate)

        workspVSCode.addSeparator()

        val workspVSCodeOpen = JMenuItem("Open VS Code Here")

        workspVSCodeOpen.addActionListener {
            VSCodeLauncher.asyncLaunch(eocvSim.workspaceManager.workspaceFile)
        }
        workspVSCode.add(workspVSCodeOpen)

        mWorkspMenu.add(workspVSCode)

        add(mWorkspMenu)

        // HELP

        val helpUsage = JMenuItem("Documentation")
        helpUsage.addActionListener {
            desktop.browse(docsUrl)
        }

        helpUsage.isEnabled = Desktop.isDesktopSupported()
        mHelpMenu.add(helpUsage)

        mHelpMenu.addSeparator()

        val helpExportLogs = JMenuItem("Export logs")
        helpExportLogs.addActionListener {
            var crashReport: CrashReport

            try {
                throw Exception("Dummy exception, log exported from GUI")
            } catch (e: Exception) {
                crashReport = CrashReport(e, isDummy = true)
            }

            DialogFactory.createFileChooser(visualizer.frame,
                DialogFactory.FileChooser.Mode.SAVE_FILE_SELECT,
                CrashReport.defaultCrashFileName, FileFilters.logFileFilter
            ).addCloseListener { OPTION, selectedFile, _ ->
                    if(OPTION == JFileChooser.APPROVE_OPTION) {
                        var path = selectedFile.absolutePath
                        if (path.endsWith(File.separator))
                            path = path.removeSuffix(File.separator)

                        crashReport.saveCrashReport(File(
                            if(!path.endsWith(".log")) {
                                "$path.log"
                            } else path
                        ))
                    }
                }
        }

        mHelpMenu.add(helpExportLogs)

        mHelpMenu.addSeparator()

        val helpIAmA = JMenuItem("I am a...")
        helpIAmA.addActionListener { DialogFactory.createIAmA(eocvSim.visualizer) }

        mHelpMenu.add(helpIAmA)

        if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
            desktop.setAboutHandler { DialogFactory.createAboutDialog(eocvSim) }
        }
        else {
            val helpAbout = JMenuItem("About")
            helpAbout.addActionListener { DialogFactory.createAboutDialog(eocvSim) }
            mHelpMenu.add(helpAbout)
        }

        add(mHelpMenu)
    }

}
