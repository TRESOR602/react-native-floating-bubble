declare module '@pelumi_coder/react-native-floating-bubble' {
  export function requestPermission(): Promise<boolean>;
  export function initialize(): Promise<void>;
  export function showFloatingBubble(x: number, y: number): Promise<void>;
  export function hideFloatingBubble(): Promise<void>;
}
