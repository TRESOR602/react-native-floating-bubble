declare module '@pelumi_coder/react-native-floating-bubble' {
  export function reopenApp(): Promise<void>;
  export function showFloatingBubble(x?: number, y?: number): Promise<void>;
  export function hideFloatingBubble(): Promise<void>;
  export function checkPermission(): Promise<boolean>;
  export function requestPermission(): Promise<boolean>;
  export function initialize(): Promise<void>;

  const _default: {
    reopenApp: typeof reopenApp;
    showFloatingBubble: typeof showFloatingBubble;
    hideFloatingBubble: typeof hideFloatingBubble;
    checkPermission: typeof checkPermission;
    requestPermission: typeof requestPermission;
    initialize: typeof initialize;
  };

  export default _default;
}
