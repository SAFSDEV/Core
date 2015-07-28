@ECHO OFF
@CLS
staf local safsvars set safs/hook/inputrecord value SHUTDOWN_HOOK
staf local sem event safs/droiddispatch post

