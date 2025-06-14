const path = require('path');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const glob = require('glob');

const scssEntries = {};
glob.sync('./src/scss/**/*.scss').forEach(file => {
    const name = path.basename(file, '.scss');
    scssEntries[name] = file;
});

module.exports = {
    // mode is set via CLI (--mode development or production)
    entry: scssEntries,
    output: {
        path: path.resolve(__dirname, 'src/css'),
        filename: '[name].js', // We don't use the JS file, but Webpack requires it
        clean: true // Clean output before build
    },
    module: {
        rules: [
            {
                test: /\.scss$/,
                use: [
                    MiniCssExtractPlugin.loader,
                    'css-loader',
                    'sass-loader'
                ]
            }
        ]
    },
    plugins: [
        new MiniCssExtractPlugin({
            filename: '[name].css'
        })
    ],
    stats: 'minimal', // Show minimal build info
    infrastructureLogging: {
        level: 'warn', // Show rebuild messages
    },
    watchOptions: {
        aggregateTimeout: 300, // Delay before rebuild
        ignored: /node_modules/,
    }
};
