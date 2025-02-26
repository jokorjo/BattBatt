import React, { useEffect } from "react";
import Quagga from "quagga";

const Scanner = ({ onDetected }) => {
  useEffect(() => {
    Quagga.init(
      {
        inputStream: { type: "LiveStream", constraints: { facingMode: "environment" } },
        decoder: { readers: ["code_128_reader", "ean_reader", "qr_reader"] },
      },
      (err) => {
        if (!err) {
          Quagga.start();
        }
      }
    );

    Quagga.onDetected((data) => {
      onDetected(data.codeResult.code);
    });

    return () => {
      Quagga.stop();
    };
  }, [onDetected]);

  return <div id="scanner" />;
};

export default Scanner;
