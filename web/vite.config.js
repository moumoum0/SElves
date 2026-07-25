import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
export default defineConfig({
    plugins: [react()],
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
