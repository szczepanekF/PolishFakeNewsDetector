export function formatDateToYMDHis(dateString) {
    const date = new Date(dateString);

    const Y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0'); // Months are zero-based
    const d = String(date.getDate()).padStart(2, '0');
    const H = String(date.getHours()).padStart(2, '0');
    const i = String(date.getMinutes()).padStart(2, '0');
    const s = String(date.getSeconds()).padStart(2, '0');

    return `${Y}-${m}-${d} ${H}:${i}:${s}`;
}