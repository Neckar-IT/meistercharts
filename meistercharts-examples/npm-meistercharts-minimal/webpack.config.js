const path = require('path');

module.exports = {
  entry: './src/main.js', // Der Einstiegspunkt Ihrer Anwendung (main.js)
  output: {
    filename: 'bundle.js', // Der Name der Ausgabedatei
    path: path.resolve(__dirname, 'dist') // Der Ausgabeordner für die generierten Dateien
  }
};
