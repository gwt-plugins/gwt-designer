package test1;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipsercp.book.tools.SamplesManagerView;

public class OpenSamplesViewHandler extends AbstractHandler
{
	/* $if eclipse.version >= 3.0 $ */
	public OpenSamplesViewHandler() {

	/* $elseif eclipse.version < 3.0 $
	public OpenSamplesViewHandler(org.eclipse.core.runtime.IPluginDescriptor descriptor) {
		super(descriptor);
	
	$endif$ */
		instance = this;
	}
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		IWorkbenchWindow window;
		/* $if eclipse.version > 3.2 $ */
		window = org.eclipse.ui.handlers.HandlerUtil.getActiveWorkbenchWindow(event);
		/* $else$
		window = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow(); 
		$endif$ */
		
		try {
			window.getActivePage().showView(SamplesManagerView.ID);
		}
		catch (PartInitException e) {
			MessageDialog.openError(window.getShell(), "Error", "Error opening the QualityEclipse Book Samples view. "
				+ e.getMessage());
		}
		return null;
	}
}
