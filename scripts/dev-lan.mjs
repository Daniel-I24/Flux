// Starts web (HTTPS on 0.0.0.0) + server and prints the LAN URLs for a real phone on Wi-Fi.
import { spawn } from 'node:child_process';
import { networkInterfaces } from 'node:os';

const WEB_PORT = 5173;
const PRIVATE_RANGES = [/^192\.168\./, /^10\./, /^172\.(1[6-9]|2\d|3[01])\./];

const lanAddresses = () =>
  Object.values(networkInterfaces())
    .flat()
    .filter((net) => net && net.family === 'IPv4' && !net.internal)
    .map((net) => net.address)
    .sort((a, b) => Number(PRIVATE_RANGES.some((r) => r.test(b))) - Number(PRIVATE_RANGES.some((r) => r.test(a))));

const addresses = lanAddresses();
const primary = addresses[0];
const origins = addresses.map((ip) => `https://${ip}:${WEB_PORT}`);

process.stdout.write('\n  FLUX en la red local (HTTPS con certificado autofirmado)\n');
if (!primary) process.stdout.write('  No se encontró una IP de red local. ¿Estás conectado a Wi-Fi?\n');
origins.forEach((origin) => {
  process.stdout.write(`  Reloj (laptop):  ${origin}/watch\n`);
  process.stdout.write(`  Celular:         ${origin}/phone\n`);
});
process.stdout.write(
  [
    '',
    '  En el celular: abre la URL, toca "Configuración avanzada" > "Continuar al sitio (no seguro)".',
    '  Es normal: el certificado es local. Después escanea el QR que muestra el reloj.',
    '',
  ].join('\n') + '\n'
);

const env = {
  ...process.env,
  VITE_PUBLIC_ORIGIN: primary ? `https://${primary}:${WEB_PORT}` : '',
  CORS_ORIGINS: ['http://localhost:5173', 'https://localhost:5173', ...origins].join(','),
};

const child = spawn('npx', ['concurrently', '-k', '-n', 'web,server', '-c', 'cyan,magenta', '"vite --mode lan"', '"npm --prefix server run dev"'], {
  env,
  stdio: 'inherit',
  shell: true,
});
child.on('exit', (code) => process.exit(code ?? 0));
