import type { Config } from 'tailwindcss'

const config: Config = {
  content: ['./src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#f0f0ff',
          100: '#e0e0ff',
          200: '#c7c7ff',
          300: '#a5a5ff',
          400: '#8b8bff',
          500: '#6366f1',
          600: '#4f46e5',
          700: '#4338ca',
          800: '#3730a3',
          900: '#312e81',
        },
        pastel: {
          mint: '#c8f7dc',
          sky: '#bae6fd',
          peach: '#fed7aa',
          lilac: '#ddd6fe',
          rose: '#fecdd3',
          lemon: '#fef9c3',
        },
        surface: {
          DEFAULT: '#fafbfe',
          card: '#ffffff',
          muted: '#f1f5f9',
        },
        text: {
          DEFAULT: '#1e293b',
          muted: '#64748b',
          subtle: '#94a3b8',
        },
      },
      fontFamily: {
        sans: ['"Noto Sans JP"', 'system-ui', 'sans-serif'],
      },
      borderRadius: {
        '4xl': '2rem',
        '5xl': '2.5rem',
      },
      animation: {
        float: 'float 20s ease-in-out infinite',
        'float-slow': 'float-slow 30s ease-in-out infinite',
        blob: 'blob 12s ease-in-out infinite',
      },
      keyframes: {
        float: {
          '0%, 100%': { transform: 'translate(0, 0)' },
          '50%': { transform: 'translate(10px, -10px)' },
        },
        'float-slow': {
          '0%, 100%': { transform: 'translate(0, 0) rotate(0deg)' },
          '33%': { transform: 'translate(5px, -8px) rotate(1deg)' },
          '66%': { transform: 'translate(-5px, 5px) rotate(-1deg)' },
        },
        blob: {
          '0%, 100%': { borderRadius: '60% 40% 30% 70% / 60% 30% 70% 40%' },
          '50%': { borderRadius: '30% 60% 70% 40% / 50% 60% 30% 60%' },
        },
      },
    },
  },
  plugins: [],
}
export default config
