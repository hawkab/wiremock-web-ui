import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
    plugins: [react()],
    resolve: {
        dedupe: ["react", "react-dom"]
    },
    build: {
        outDir: "dist",
        rollupOptions: {
            output: {
                banner: `/* @author olshansky (c) 2026 */`,
            },
        },
    }
});
