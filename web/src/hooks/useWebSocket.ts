import { useCallback, useEffect, useRef } from 'react';
import { getApiBaseUrl } from '../lib/api';

export interface WebSocketEvent {
  type: string;
  data: unknown;
}

interface UseWebSocketOptions {
  onEvent: (event: WebSocketEvent) => void;
  enabled?: boolean;
}

export function useWebSocket({ onEvent, enabled = true }: UseWebSocketOptions) {
  const wsRef = useRef<WebSocket | null>(null);
  const reconnectTimer = useRef<ReturnType<typeof setTimeout>>(undefined);
  const reconnectDelay = useRef(1000);
  const onEventRef = useRef(onEvent);
  const shouldReconnectRef = useRef(false);
  onEventRef.current = onEvent;

  const connect = useCallback(() => {
    if (!enabled) return;

    const baseUrl = getApiBaseUrl();
    const wsUrl = baseUrl.replace(/^http/, 'ws') + '/ws';

    shouldReconnectRef.current = true;

    try {
      const ws = new WebSocket(wsUrl);
      wsRef.current = ws;

      ws.onopen = () => {
        reconnectDelay.current = 1000;
      };

      ws.onmessage = (event) => {
        try {
          const parsed = JSON.parse(event.data as string) as WebSocketEvent;
          onEventRef.current(parsed);
        } catch { /* ignore malformed */ }
      };

      ws.onclose = () => {
        wsRef.current = null;
        if (!shouldReconnectRef.current) return;
        reconnectTimer.current = setTimeout(() => {
          reconnectDelay.current = Math.min(reconnectDelay.current * 2, 30000);
          connect();
        }, reconnectDelay.current);
      };

      ws.onerror = () => {
        ws.close();
      };
    } catch { /* ignore */ }
  }, [enabled]);

  useEffect(() => {
    connect();
    return () => {
      shouldReconnectRef.current = false;
      clearTimeout(reconnectTimer.current);
      wsRef.current?.close();
      wsRef.current = null;
    };
  }, [connect]);

  const disconnect = useCallback(() => {
    shouldReconnectRef.current = false;
    clearTimeout(reconnectTimer.current);
    wsRef.current?.close();
    wsRef.current = null;
  }, []);

  return { disconnect };
}