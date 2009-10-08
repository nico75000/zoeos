package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.gui.TitleProvider;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.gui.ComponentGenerationException;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.gui.desktop.*;
import com.pcmsolutions.system.IllegalStateTransitionException;
import com.pcmsolutions.system.ZDeviceCannotBeRemovedException;
import com.pcmsolutions.system.ZExternalDevice;
import com.pcmsolutions.system.callback.CallbackTask;
import com.pcmsolutions.system.callback.CallbackTaskResult;
import com.pcmsolutions.system.callback.CallbackTaskRunnable;
import com.pcmsolutions.system.paths.ViewPath;
import com.pcmsolutions.system.threads.ZDefaultThread;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.MutableTreeNode;
import java.io.Serializable;

/**
 * User: paulmeehan
 * Date: 20-Jan-2004
 * Time: 10:20:29
 */
class ViewMediator {
    public static interface ViewTask {
        public CallbackTaskResult open(boolean activate);

        public CallbackTaskResult open();

        public CallbackTaskResult close();
    }

    private static abstract class AbstractViewTask extends ViewCallbackTask implements ViewTask {
        protected DesktopElement desktopElement;

        public void init(DesktopElement desktopElement) {
            this.desktopElement = desktopElement;
        }

        // result is Boolean - false for view already open, true for view opened
        public CallbackTaskResult open(boolean activate) {
            return openView(desktopElement, activate);
        }

        // result is Boolean - false for view already open, true for view opened
        public CallbackTaskResult open() {
            return openView(desktopElement, true);
        }

        public CallbackTaskResult close() {
            return closeView(desktopElement);
        }
    }

    private static class DeviceCloseBehaviour implements ActivityContext, Serializable {
        private DeviceContext device;

        public DeviceCloseBehaviour(DeviceContext device) {
            this.device = device;
        }

        public boolean tryClosing() {
            new ZDefaultThread() {
                public void run() {
                    try {
                        if (device.getState() != ZExternalDevice.STATE_REMOVED)
                            if (UserMessaging.askYesNo("Remove device " + device.getName() + " ?")) {
                                if (device.getState() == ZExternalDevice.STATE_RUNNING)
                                    device.stopDevice(true, "User request");
                                device.removeDevice(true);
                            }
                    } catch (IllegalStateTransitionException e1) {
                        e1.printStackTrace();
                    } catch (ZDeviceCannotBeRemovedException e1) {
                        e1.printStackTrace();
                    }
                }
            }.start();
            return false;
        }

        public void closed() {
        }

        public void activated() {
        }

        public void deactivated() {
        }
    }

    public static class TaskDeviceWorkspace extends AbstractViewTask {
        public TaskDeviceWorkspace(final DeviceContext device) {
            init(makeDeviceWorkspaceDesktopElement(device, ViewFactory.provideDefaultView(device),
                    false, new DeviceCloseBehaviour(device), new Impl_DesktopNodeDescriptor(new Object[]{device}, false)));
        }
        TreeNode t;
        MutableTreeNode mtn;
    }

    public static class TaskPreset extends AbstractViewTask {
        private static final String asChildTitle = "Main";
        private static final String asChildReducedTitle = "Main";

        public TaskPreset(final ContextEditablePreset p) {
            init(makeDeviceWorkspaceDesktopElement(p.getDeviceContext(), ViewFactory.provideDefaultView(p),
                    false, StaticActivityContext.TRUE, new Impl_DesktopNodeDescriptor(new Object[]{p}, asChildTitle, asChildReducedTitle)));
        }

        public TaskPreset(final ReadablePreset p) {
            init(makeDeviceWorkspaceDesktopElement(p.getDeviceContext(), ViewFactory.provideDefaultView(p),
                    false, StaticActivityContext.TRUE, new Impl_DesktopNodeDescriptor(new Object[]{p}, asChildTitle, asChildReducedTitle)));
        }
    }

    public static class TabbedTaskVoice implements ViewTask {
        private TaskVoice[] tasks;

        public TabbedTaskVoice(final ReadablePreset.ReadableVoice voice, boolean groupEnvelopes) {
            if (groupEnvelopes) {
                tasks = new TaskVoice[6];
                tasks[0] = new TaskVoice(voice, true);
                tasks[1] = new TaskVoice(voice, VoiceSections.VOICE_TUNING, VoiceSections.voiceTuningTitle);
                tasks[2] = new TaskVoice(voice, VoiceSections.VOICE_AMP_FILTER, VoiceSections.voiceAmpFilterTitle);
                //tasks[2] = new TaskVoice(voice, VoiceSections.VOICE_FILTER, VoiceSections.voiceFilterTitle);
                tasks[3] = new TaskVoice(voice, VoiceSections.VOICE_ENVELOPES, VoiceSections.voiceEnvelopesTitle);
                tasks[4] = new TaskVoice(voice, VoiceSections.VOICE_LFO, VoiceSections.voiceLfoTitle);
                tasks[5] = new TaskVoice(voice, VoiceSections.VOICE_CORDS, VoiceSections.voiceCordsTitle);
            } else {
                tasks = new TaskVoice[8];
                tasks[0] = new TaskVoice(voice, true);
                tasks[1] = new TaskVoice(voice, VoiceSections.VOICE_TUNING, VoiceSections.voiceTuningTitle);
                tasks[2] = new TaskVoice(voice, VoiceSections.VOICE_AMP_FILTER, VoiceSections.voiceAmpFilterTitle);
                //tasks[2] = new TaskVoice(voice, VoiceSections.VOICE_FILTER, VoiceSections.voiceFilterTitle);
                tasks[3] = new TaskVoice(voice, VoiceSections.VOICE_AMP_ENVELOPE, VoiceSections.voiceAmpEnvelopeTitle);
                tasks[4] = new TaskVoice(voice, VoiceSections.VOICE_FILTER_ENVELOPE, VoiceSections.voiceFilterEnvelopeTitle);
                tasks[5] = new TaskVoice(voice, VoiceSections.VOICE_AUX_ENVELOPE, VoiceSections.voiceAuxEnvelopeTitle);
                tasks[6] = new TaskVoice(voice, VoiceSections.VOICE_LFO, VoiceSections.voiceLfoTitle);
                tasks[7] = new TaskVoice(voice, VoiceSections.VOICE_CORDS, VoiceSections.voiceCordsTitle);
            }
        }

        public TabbedTaskVoice(final ContextEditablePreset.EditableVoice voice, boolean groupEnvelopes) {
            this(new ContextEditablePreset.EditableVoice[]{voice}, groupEnvelopes);
        }

        public TabbedTaskVoice(final ContextEditablePreset.EditableVoice[] voices, boolean groupEnvelopes) {
            if (groupEnvelopes) {
                tasks = new TaskVoice[6];
                tasks[0] = new TaskVoice(voices, true);
                tasks[1] = new TaskVoice(voices, VoiceSections.VOICE_TUNING, VoiceSections.voiceTuningTitle);
                tasks[2] = new TaskVoice(voices, VoiceSections.VOICE_AMP_FILTER, VoiceSections.voiceAmpFilterTitle);
                //tasks[2] = new TaskVoice(voices, VoiceSections.VOICE_FILTER, VoiceSections.voiceFilterTitle);
                tasks[3] = new TaskVoice(voices, VoiceSections.VOICE_ENVELOPES, VoiceSections.voiceEnvelopesTitle);
                tasks[4] = new TaskVoice(voices, VoiceSections.VOICE_LFO, VoiceSections.voiceLfoTitle);
                tasks[5] = new TaskVoice(voices, VoiceSections.VOICE_CORDS, VoiceSections.voiceCordsTitle);
            } else {
                tasks = new TaskVoice[8];
                tasks[0] = new TaskVoice(voices, true);
                tasks[1] = new TaskVoice(voices, VoiceSections.VOICE_TUNING, VoiceSections.voiceTuningTitle);
                tasks[2] = new TaskVoice(voices, VoiceSections.VOICE_AMP_FILTER, VoiceSections.voiceAmpFilterTitle);
                //tasks[2] = new TaskVoice(voices, VoiceSections.VOICE_FILTER, VoiceSections.voiceFilterTitle);
                tasks[3] = new TaskVoice(voices, VoiceSections.VOICE_AMP_ENVELOPE, VoiceSections.voiceAmpEnvelopeTitle);
                tasks[4] = new TaskVoice(voices, VoiceSections.VOICE_FILTER_ENVELOPE, VoiceSections.voiceFilterEnvelopeTitle);
                tasks[5] = new TaskVoice(voices, VoiceSections.VOICE_AUX_ENVELOPE, VoiceSections.voiceAuxEnvelopeTitle);
                tasks[6] = new TaskVoice(voices, VoiceSections.VOICE_LFO, VoiceSections.voiceLfoTitle);
                tasks[7] = new TaskVoice(voices, VoiceSections.VOICE_CORDS, VoiceSections.voiceCordsTitle);
            }
        }

        public CallbackTaskResult open(boolean activate) {
            CallbackTaskResult rv = null;
            for (int i = 0; i < tasks.length; i++)
                if (i == 0) {
                    rv = tasks[i].open(activate);
                    if (!rv.suceeded() || (rv.suceeded() && !((Boolean) rv.getResult()).booleanValue()))
                        return rv;
                } else if (i == 1)
                    rv = tasks[i].open(activate);
                else
                    rv = tasks[i].open(false);
            return rv;
        }

        public CallbackTaskResult open() {
            return open(false);
        }

        public CallbackTaskResult close() {
            CallbackTaskResult rv = null;
            for (int i = 0; i < tasks.length; i++)
                rv = tasks[i].close();
            return rv;
        }
    }

    public static class TaskVoice extends AbstractViewTask {
        protected TaskVoice(final ReadablePreset.ReadableVoice voice, boolean empty) {
            Impl_DesktopNodeDescriptor nd = new Impl_DesktopNodeDescriptor(false);
            nd.setShowingCloseButton(false);
            init(makeDeviceWorkspaceDesktopElement(voice.getPreset().getDeviceContext(), ViewFactory.provideDefaultView(voice, empty),
                    false, StaticActivityContext.TRUE,
                    nd));
        }

        public TaskVoice(final ReadablePreset.ReadableVoice voice) {
            this(voice, false);
        }

        public TaskVoice(final ReadablePreset.ReadableVoice voice, final int sections, final TitleProvider tp) {
            init(makeDeviceWorkspaceDesktopElement(voice.getPreset().getDeviceContext(), ViewFactory.provideDefaultView(voice, sections, tp),
                    false, StaticActivityContext.FALSE,
                    new Impl_DesktopNodeDescriptor(false)));
        }

        public TaskVoice(final ContextEditablePreset.EditableVoice voice) {
            this(voice, false);
        }

        protected TaskVoice(final ContextEditablePreset.EditableVoice voice, boolean empty) {
            this(new ContextEditablePreset.EditableVoice[]{voice}, empty);
        }

        public TaskVoice(final ContextEditablePreset.EditableVoice voice, int sections, TitleProvider tp) {
            this(new ContextEditablePreset.EditableVoice[]{voice}, sections, tp);
        }

        public TaskVoice(final ContextEditablePreset.EditableVoice[] voices, final int sections, final TitleProvider tp) {
            init(makeDeviceWorkspaceDesktopElement(voices[0].getPreset().getDeviceContext(), ViewFactory.provideDefaultView(voices, sections, tp),
                    false, StaticActivityContext.FALSE,
                    new Impl_DesktopNodeDescriptor(false)));
        }

        public TaskVoice(final ContextEditablePreset.EditableVoice[] voices) {
            this(voices, false);
        }

        protected TaskVoice(final ContextEditablePreset.EditableVoice[] voices, boolean empty) {
            Impl_DesktopNodeDescriptor nd = new Impl_DesktopNodeDescriptor(false);
            nd.setShowingCloseButton(false);
            init(makeDeviceWorkspaceDesktopElement(voices[0].getPreset().getDeviceContext(), ViewFactory.provideDefaultView(voices, empty),
                    false, StaticActivityContext.TRUE,
                    nd));
        }
    }

    public static class TaskDevice extends AbstractViewTask {
        public TaskDevice(final DeviceContext device) {
            init(makeDeviceDesktopElement(device, ViewFactory.provideDeviceView(device),
                    true, StaticActivityContext.FALSE, new Impl_DesktopNodeDescriptor(false)));
        }
    }

    public static class TaskProperties extends AbstractViewTask {
        public TaskProperties(final DeviceContext device) {
            init(makeDeviceDesktopElement(device, ViewFactory.providePropertiesView(device),
                    true, StaticActivityContext.FALSE, new Impl_DesktopNodeDescriptor(false)));
        }
    }

    public static class TaskDefaultPresetContext extends AbstractViewTask {
        public TaskDefaultPresetContext(final DeviceContext device) {
            init(makeDeviceDesktopElement(device, ViewFactory.provideDefaultPresetContextView(device),
                    true, StaticActivityContext.FALSE, new Impl_DesktopNodeDescriptor(false)));
        }
    }

    public static class TaskDefaultSampleContext extends AbstractViewTask {
        public TaskDefaultSampleContext(final DeviceContext device) {
            init(makeDeviceDesktopElement(device, ViewFactory.provideDefaultSampleContextView(device),
                    true, StaticActivityContext.FALSE, new Impl_DesktopNodeDescriptor(false)));
        }
    }

    public static class TaskMultiMode extends AbstractViewTask {
        public TaskMultiMode(final DeviceContext device) {
            init(makeDeviceDesktopElement(device, ViewFactory.provideMultiModeView(device),
                    true, StaticActivityContext.FALSE, new Impl_DesktopNodeDescriptor(false)));
        }
    }

    public static class TaskMaster extends AbstractViewTask {
        public TaskMaster(DeviceContext device) {
            init(makeDeviceDesktopElement(device, ViewFactory.provideMasterView(device),
                    true, StaticActivityContext.FALSE, new Impl_DesktopNodeDescriptor(false)));
        }
    }

    public static void openDesktopElements(final DesktopElement[] elems) {
        CallbackTask ct = new CallbackTask();
        for (int i = 0; i < elems.length; i++) {
            final DesktopElement elem = elems[i];
            ct.init(new CallbackTaskRunnable() {
                public Object run() throws ComponentGenerationException, ChildViewNotAllowedException, LogicalHierarchyException {
                    return new Boolean(ZoeosFrame.getInstance().getZDesktopManager().addDesktopElement(elem));
                }
            });
            CallbackTaskResult ctr = ct.execute();
        }
    }

    public static void modifyBranch(final DesktopBranch branch, final boolean activate, final int clipIndex) {
        CallbackTask ct = new CallbackTask();
        ct.init(new CallbackTaskRunnable() {
            public Object run() throws ComponentGenerationException, ChildViewNotAllowedException, LogicalHierarchyException {
                ZoeosFrame.getInstance().getZDesktopManager().modifyBranch(branch, activate, clipIndex);
                return null;
            }
        });
        CallbackTaskResult ctr = ct.execute();
    }

    public static Object executeViewTask(CallbackTaskRunnable ctr) throws Exception {
        CallbackTask ct = new CallbackTask();
        ct.init(ctr);
        CallbackTaskResult ctres = ct.execute();
        if (ctres.suceeded())
            return ctres.getResult();
        else
            throw ctres.getException();
    }

    public static DeviceDesktopElement[] getDeviceDesktopElements(final DeviceContext device) throws Exception {
        DesktopElement[] e = (DesktopElement[]) executeViewTask(new CallbackTaskRunnable() {
            public Object run() throws Exception {
                return ZoeosFrame.getInstance().getZDesktopManager().getDesktopElementTree(new ViewPath(ZDesktopManager.dockWORKSPACE, ViewFactory.provideDefaultDesktopNames(device)));
            }
        });
        DeviceDesktopElement[] de = new DeviceDesktopElement[e.length];
        System.arraycopy(e, 0, de, 0, e.length);

        return de;
    }

    private static class DeviceDesktopElement extends AbstractDesktopElement {
        private DeviceContext device;
        private ViewInstance viewInstance;

        public DeviceDesktopElement(ViewInstance vi, boolean floatable, ActivityContext activityContext, DesktopNodeDescriptor nodalDescriptor, DeviceContext device) {
            super(vi.getViewPath(), floatable, activityContext, nodalDescriptor);
            this.device = device;
            viewInstance = vi;
        }

        protected JComponent createView() throws ComponentGenerationException {
            return viewInstance.getView();
        }

        public boolean hasExpired() {
            return device.getState() == ZExternalDevice.STATE_REMOVED;
        }

        public DesktopElement getCopy() {
            DeviceDesktopElement clone = new DeviceDesktopElement(viewInstance, floatable, activityContext, nodalDescriptor, device);
            clone.sessionString = this.sessionString;
            return clone;
        }
    }

    private static class DeviceWorkspaceDesktopElement extends DeviceDesktopElement {
        public DeviceWorkspaceDesktopElement(ViewInstance vi, boolean floatable, ActivityContext activityContext, DesktopNodeDescriptor nodalDescriptor, DeviceContext device) {
            super(vi, floatable, new ActivityContextWrapper(activityContext, device), nodalDescriptor, device);
        }

        private static class ActivityContextWrapper implements ActivityContext {
            private ActivityContext enclosedAC;
            private DeviceContext device;

            public ActivityContextWrapper(ActivityContext enclosedAC, DeviceContext device) {
                this.enclosedAC = enclosedAC;
                this.device = device;
            }

            public boolean tryClosing() {
                return enclosedAC.tryClosing();
            }

            public void closed() {
                enclosedAC.closed();
            }

            public void activated() {
                enclosedAC.activated();
                if (device.getDevicePreferences().ZPREF_syncPalettes.getValue())
                    device.getViewManager().activateDevicePalettes().start();
            }

            public void deactivated() {
                enclosedAC.deactivated();
            }
        }
    }

    private static class ViewCallbackTask extends CallbackTask {
        // create
        public static DesktopElement makeDeviceDesktopElement(final DeviceContext device, final ViewInstance vi, final boolean isFloatable, final ActivityContext activityContext, final DesktopNodeDescriptor nd) {
            return new DeviceDesktopElement(vi, isFloatable, activityContext, nd, device);
        }

        public static DesktopElement makeDeviceWorkspaceDesktopElement(final DeviceContext device, final ViewInstance vi, final boolean isFloatable, final ActivityContext activityContext, final DesktopNodeDescriptor nd) {
            return new DeviceWorkspaceDesktopElement(vi, isFloatable, activityContext, nd, device);
        }

        protected CallbackTaskResult openView(final DesktopElement desktopElement, final boolean activate) {
            init(new CallbackTaskRunnable() {
                public Object run() throws ComponentGenerationException, ChildViewNotAllowedException, LogicalHierarchyException {
                    return new Boolean(ZoeosFrame.getInstance().getZDesktopManager().addDesktopElement(desktopElement, activate));
                }
            });
            return execute();
        }

        // closed
        protected CallbackTaskResult closeView(final DesktopElement desktopElement) {
            init(new CallbackTaskRunnable() {
                public Object run() throws ComponentGenerationException, ChildViewNotAllowedException, LogicalHierarchyException {
                    return new Boolean(ZoeosFrame.getInstance().getZDesktopManager().removeDesktopElement(desktopElement));
                }
            });
            return execute();
        }

        protected CallbackTaskResult closeView(final ViewPath vp) {
            init(new CallbackTaskRunnable() {
                public Object run() throws ComponentGenerationException, ChildViewNotAllowedException, LogicalHierarchyException {
                    return new Boolean(ZoeosFrame.getInstance().getZDesktopManager().removeElement(vp));
                }
            });
            return execute();
        }
    }
}

