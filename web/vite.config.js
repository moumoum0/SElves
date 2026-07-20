import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
export default defineConfig({
    plugins: [react()],
    test: {
        environment: 'jsdom',
        setupFiles: ['./src/test/setup.ts'],
        css: true,
        coverage: {
            provider: 'v8',
            reporter: ['text', 'html', 'lcov'],
            reportsDirectory: './coverage',
        },
    },
    server: {
        host: '0.0.0.0',
        port: 5173,
    },
    build: {
        rollupOptions: {
            output: {
                manualChunks(id) {
                    if (id.includes('/node_modules/react') || id.includes('/node_modules/react-dom') || id.includes('/node_modules/react-router')) {
                        return 'vendor-react';
                    }
                    if (id.includes('/node_modules/mdui')) {
                        return 'vendor-mdui';
                    }
                    if (id.includes('/node_modules/@material/web')) {
                        return 'vendor-material';
                    }
                },
            },
        },
    },
});
