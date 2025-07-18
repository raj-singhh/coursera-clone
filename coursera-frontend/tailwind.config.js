/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}", // This line is crucial for Tailwind to scan your Angular templates
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}