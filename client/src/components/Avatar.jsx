import { useEffect, useState } from "react";
import { api } from "../api.js";

export default function Avatar({ path, version = 0, size = 48 }) {
    const [src, setSrc] = useState(null);

    useEffect(() => {
        let url = null;
        let active = true;
        api.image(path).then((blobUrl) => {
            url = blobUrl;
            if (active) setSrc(blobUrl);
        });
        return () => {
            active = false;
            if (url) URL.revokeObjectURL(url);
        };
    }, [path, version]);

    return <img className="avatar" src={src || "/default-user.png"} width={size} height={size} alt="" />;
}
