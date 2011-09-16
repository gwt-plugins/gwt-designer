
#include "MyWebUIAdapter.h"

@implementation MyWebUIAdapter

- (void)webView:(WebView *)sender runJavaScriptAlertPanelWithMessage:(NSString *)message initiatedByFrame:(WebFrame *)frame
{
	printf("alert(): %s\n", [message UTF8String]);fflush(stdout);
}
@end