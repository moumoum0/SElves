// vite.config.js
import { defineConfig } from "file:///D:/project/selves/web/node_modules/vite/dist/node/index.js";
import react from "file:///D:/project/selves/web/node_modules/@vitejs/plugin-react/dist/index.js";
var vite_config_default = defineConfig({
  plugins: [react()],
  server: {
    host: "0.0.0.0",
    port: 5173
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes("/node_modules/react") || id.includes("/node_modules/react-dom") || id.includes("/node_modules/react-router")) {
            return "vendor-react";
          }
          if (id.includes("/node_modules/mdui")) {
            return "vendor-mdui";
          }
          if (id.includes("/node_modules/@material/web")) {
            return "vendor-material";
          }
        }
      }
    }
  }
});
export {
  vite_config_default as default
};
//# sourceMappingURL=data:application/json;base64,ewogICJ2ZXJzaW9uIjogMywKICAic291cmNlcyI6IFsidml0ZS5jb25maWcuanMiXSwKICAic291cmNlc0NvbnRlbnQiOiBbImNvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9kaXJuYW1lID0gXCJEOlxcXFxwcm9qZWN0XFxcXHNlbHZlc1xcXFx3ZWJcIjtjb25zdCBfX3ZpdGVfaW5qZWN0ZWRfb3JpZ2luYWxfZmlsZW5hbWUgPSBcIkQ6XFxcXHByb2plY3RcXFxcc2VsdmVzXFxcXHdlYlxcXFx2aXRlLmNvbmZpZy5qc1wiO2NvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9pbXBvcnRfbWV0YV91cmwgPSBcImZpbGU6Ly8vRDovcHJvamVjdC9zZWx2ZXMvd2ViL3ZpdGUuY29uZmlnLmpzXCI7aW1wb3J0IHsgZGVmaW5lQ29uZmlnIH0gZnJvbSAndml0ZSc7XG5pbXBvcnQgcmVhY3QgZnJvbSAnQHZpdGVqcy9wbHVnaW4tcmVhY3QnO1xuZXhwb3J0IGRlZmF1bHQgZGVmaW5lQ29uZmlnKHtcbiAgICBwbHVnaW5zOiBbcmVhY3QoKV0sXG4gICAgc2VydmVyOiB7XG4gICAgICAgIGhvc3Q6ICcwLjAuMC4wJyxcbiAgICAgICAgcG9ydDogNTE3MyxcbiAgICB9LFxuICAgIGJ1aWxkOiB7XG4gICAgICAgIHJvbGx1cE9wdGlvbnM6IHtcbiAgICAgICAgICAgIG91dHB1dDoge1xuICAgICAgICAgICAgICAgIG1hbnVhbENodW5rcyhpZCkge1xuICAgICAgICAgICAgICAgICAgICBpZiAoaWQuaW5jbHVkZXMoJy9ub2RlX21vZHVsZXMvcmVhY3QnKSB8fCBpZC5pbmNsdWRlcygnL25vZGVfbW9kdWxlcy9yZWFjdC1kb20nKSB8fCBpZC5pbmNsdWRlcygnL25vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXInKSkge1xuICAgICAgICAgICAgICAgICAgICAgICAgcmV0dXJuICd2ZW5kb3ItcmVhY3QnO1xuICAgICAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICAgICAgICAgIGlmIChpZC5pbmNsdWRlcygnL25vZGVfbW9kdWxlcy9tZHVpJykpIHtcbiAgICAgICAgICAgICAgICAgICAgICAgIHJldHVybiAndmVuZG9yLW1kdWknO1xuICAgICAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICAgICAgICAgIGlmIChpZC5pbmNsdWRlcygnL25vZGVfbW9kdWxlcy9AbWF0ZXJpYWwvd2ViJykpIHtcbiAgICAgICAgICAgICAgICAgICAgICAgIHJldHVybiAndmVuZG9yLW1hdGVyaWFsJztcbiAgICAgICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgICAgIH0sXG4gICAgICAgICAgICB9LFxuICAgICAgICB9LFxuICAgIH0sXG59KTtcbiJdLAogICJtYXBwaW5ncyI6ICI7QUFBeVAsU0FBUyxvQkFBb0I7QUFDdFIsT0FBTyxXQUFXO0FBQ2xCLElBQU8sc0JBQVEsYUFBYTtBQUFBLEVBQ3hCLFNBQVMsQ0FBQyxNQUFNLENBQUM7QUFBQSxFQUNqQixRQUFRO0FBQUEsSUFDSixNQUFNO0FBQUEsSUFDTixNQUFNO0FBQUEsRUFDVjtBQUFBLEVBQ0EsT0FBTztBQUFBLElBQ0gsZUFBZTtBQUFBLE1BQ1gsUUFBUTtBQUFBLFFBQ0osYUFBYSxJQUFJO0FBQ2IsY0FBSSxHQUFHLFNBQVMscUJBQXFCLEtBQUssR0FBRyxTQUFTLHlCQUF5QixLQUFLLEdBQUcsU0FBUyw0QkFBNEIsR0FBRztBQUMzSCxtQkFBTztBQUFBLFVBQ1g7QUFDQSxjQUFJLEdBQUcsU0FBUyxvQkFBb0IsR0FBRztBQUNuQyxtQkFBTztBQUFBLFVBQ1g7QUFDQSxjQUFJLEdBQUcsU0FBUyw2QkFBNkIsR0FBRztBQUM1QyxtQkFBTztBQUFBLFVBQ1g7QUFBQSxRQUNKO0FBQUEsTUFDSjtBQUFBLElBQ0o7QUFBQSxFQUNKO0FBQ0osQ0FBQzsiLAogICJuYW1lcyI6IFtdCn0K
